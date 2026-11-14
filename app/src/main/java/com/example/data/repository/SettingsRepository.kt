package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "alhuda_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_CALC_METHOD = intPreferencesKey("calc_method")
        val KEY_JURISTIC_SCHOOL = intPreferencesKey("juristic_school")
        val KEY_TRANSLATION_ID = stringPreferencesKey("translation_id")
        val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        val KEY_RECITER_ID = stringPreferencesKey("reciter_id")
        val KEY_CITY_NAME = stringPreferencesKey("city_name")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val KEY_CALENDAR_SYNC = booleanPreferencesKey("calendar_sync_enabled")
        val KEY_LAST_EBOOK_PAGE = intPreferencesKey("last_ebook_page")
        val KEY_EBOOK_THEME = stringPreferencesKey("ebook_theme") // "PARCHMENT", "NIGHT", "DAY"
        val KEY_EBOOK_SHOW_TRANSLATION = booleanPreferencesKey("ebook_show_translation")
        val KEY_EBOOK_FONT_SCALE = floatPreferencesKey("ebook_font_scale")
    }

    val calcMethod: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CALC_METHOD] ?: 2
    }

    val juristicSchool: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_JURISTIC_SCHOOL] ?: 0
    }

    val translationId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TRANSLATION_ID] ?: "en.sahih"
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_FONT_SCALE] ?: 1.2f
    }

    val reciterId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_RECITER_ID] ?: "ar.alafasy"
    }

    val cityName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CITY_NAME] ?: "Makkah, Saudi Arabia"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val calendarSyncEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CALENDAR_SYNC] ?: false
    }

    val lastEbookPage: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_EBOOK_PAGE] ?: 1
    }

    val ebookTheme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_EBOOK_THEME] ?: "PARCHMENT"
    }

    val ebookShowTranslation: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_EBOOK_SHOW_TRANSLATION] ?: true
    }

    val ebookFontScale: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_EBOOK_FONT_SCALE] ?: 1.3f
    }

    suspend fun setCalcMethod(method: Int) {
        context.dataStore.edit { it[KEY_CALC_METHOD] = method }
    }

    suspend fun setJuristicSchool(school: Int) {
        context.dataStore.edit { it[KEY_JURISTIC_SCHOOL] = school }
    }

    suspend fun setTranslationId(id: String) {
        context.dataStore.edit { it[KEY_TRANSLATION_ID] = id }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[KEY_FONT_SCALE] = scale }
    }

    suspend fun setReciterId(id: String) {
        context.dataStore.edit { it[KEY_RECITER_ID] = id }
    }

    suspend fun setCityName(city: String) {
        context.dataStore.edit { it[KEY_CITY_NAME] = city }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CALENDAR_SYNC] = enabled }
    }

    suspend fun setLastEbookPage(page: Int) {
        context.dataStore.edit { it[KEY_LAST_EBOOK_PAGE] = page.coerceIn(1, 604) }
    }

    suspend fun setEbookTheme(theme: String) {
        context.dataStore.edit { it[KEY_EBOOK_THEME] = theme }
    }

    suspend fun setEbookShowTranslation(show: Boolean) {
        context.dataStore.edit { it[KEY_EBOOK_SHOW_TRANSLATION] = show }
    }

    suspend fun setEbookFontScale(scale: Float) {
        context.dataStore.edit { it[KEY_EBOOK_FONT_SCALE] = scale.coerceIn(0.9f, 2.2f) }
    }
}
