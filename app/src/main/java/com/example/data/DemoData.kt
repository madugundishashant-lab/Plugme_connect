package com.example.data

import com.example.model.Contact

/**
 * Default data definitions for clean production deployment.
 * All pre-existing mock and sample data removed so the app starts with a clean slate.
 */
object DemoData {
    val DEFAULT_PROFILE = Contact()
    val DEMO_CONTACTS = emptyList<Contact>()
}
