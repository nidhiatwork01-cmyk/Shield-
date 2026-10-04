package com.scamshield.app.family

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "family_contact")

class FamilyContactRepo(private val context: Context) {
    private val CONTACT_NAME = stringPreferencesKey("contact_name")
    private val CONTACT_PHONE = stringPreferencesKey("contact_phone")

    val contactName: Flow<String?> = context.dataStore.data.map { prefs -> prefs[CONTACT_NAME] }
    val contactPhone: Flow<String?> = context.dataStore.data.map { prefs -> prefs[CONTACT_PHONE] }

    suspend fun saveContact(name: String, phone: String) {
        context.dataStore.edit { prefs ->
            prefs[CONTACT_NAME] = name
            prefs[CONTACT_PHONE] = phone
        }
    }
}
