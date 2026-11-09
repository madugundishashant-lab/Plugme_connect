package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import com.example.model.Contact
import com.example.model.PlatformCatalog

object DeviceContactsHelper {

    /**
     * Launches the system contacts app with pre-filled fields from the scanned Contact.
     * Uses Android's native ContactsContract.Intents.Insert.ACTION, requiring zero
     * sensitive runtime permissions while letting the user save to Google Contacts or Device.
     */
    fun saveToDeviceContacts(context: Context, contact: Contact): Boolean {
        val displayName = contact.n.ifBlank { contact.u.ifBlank { "New Plug" } }
        val email = contact.e
        val phone = contact.effectivePhone
        val jobTitle = contact.title
        val company = contact.company

        // Formulate a clean, comprehensive notes summary for the contact
        val notes = buildString {
            if (contact.title.isNotBlank()) {
                append("Role: ${contact.title}\n")
            }
            if (contact.company.isNotBlank()) {
                append("Company: ${contact.company}\n")
            }
            if (contact.u.isNotBlank()) {
                append("Plugme: @${contact.u}\n")
            }
            if (contact.a != null && contact.a in 1..150) {
                append("Age: ${contact.a}\n")
            }
            if (contact.g.isNotBlank()) {
                append("Gender: ${contact.g}\n")
            }

            // Social platforms (excluding phone which is in phone field)
            val webPlatforms = contact.platforms.filterKeys { it != "ph" }
            if (webPlatforms.isNotEmpty()) {
                append("\nLinks:\n")
                webPlatforms.forEach { (key, value) ->
                    val info = PlatformCatalog.get(key)
                    val url = PlatformCatalog.buildUrl(key, value)
                    append("• ${info.name}: $url\n")
                }
            }

            // Gaming entries
            if (contact.gms.isNotEmpty()) {
                append("\nGaming Tags:\n")
                contact.gms.forEach { game ->
                    append("• ${game.n}: ${game.i}\n")
                }
            }
        }.trim()

        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, displayName)

            if (jobTitle.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.JOB_TITLE, jobTitle)
            }
            if (company.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.COMPANY, company)
            }

            if (email.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.EMAIL, email)
                putExtra(
                    ContactsContract.Intents.Insert.EMAIL_TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_HOME
                )
            }

            if (phone.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                putExtra(
                    ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
            }

            if (notes.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.NOTES, notes)
            }

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to general Contacts.CONTENT_TYPE if RawContacts is not accepted
            try {
                val fallbackIntent = Intent(Intent.ACTION_INSERT).apply {
                    type = ContactsContract.Contacts.CONTENT_TYPE
                    putExtra(ContactsContract.Intents.Insert.NAME, displayName)
                    if (jobTitle.isNotBlank()) {
                        putExtra(ContactsContract.Intents.Insert.JOB_TITLE, jobTitle)
                    }
                    if (company.isNotBlank()) {
                        putExtra(ContactsContract.Intents.Insert.COMPANY, company)
                    }
                    if (email.isNotBlank()) {
                        putExtra(ContactsContract.Intents.Insert.EMAIL, email)
                    }
                    if (phone.isNotBlank()) {
                        putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                    }
                    if (notes.isNotBlank()) {
                        putExtra(ContactsContract.Intents.Insert.NOTES, notes)
                    }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (e2: Exception) {
                Toast.makeText(context, "Could not launch Contacts app", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}
