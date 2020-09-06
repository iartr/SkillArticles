package ru.skillbranch.skillarticles.data.delegates

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.squareup.moshi.JsonAdapter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PrefLiveDelegate<T>(
    private val fieldKey: String,
    private val defaultValue: T,
    private val preferences: SharedPreferences
) : ReadOnlyProperty<Any?, LiveData<T>> {
    private var storedValue: LiveData<T>? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): LiveData<T> {
        if (storedValue == null) {
            storedValue = SharedPreferenceLiveData(preferences, fieldKey, defaultValue)
        }
        return storedValue!!
    }
}


internal class SharedPreferenceLiveData<T>(
    var sharedPrefs: SharedPreferences,
    var key: String,
    var defValue: T
) : LiveData<T>() {
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, shKey ->
            if (shKey == key) {
                value = readValue(defValue)
            }
        }

    override fun onActive() {
        super.onActive()
        value = readValue(defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }

    @Suppress("UNCHECKED_CAST")
    private fun readValue(defaultValue: T): T {
        return when (defaultValue) {
            is Int -> sharedPrefs.getInt(key, defaultValue as Int) as T
            is Long -> sharedPrefs.getLong(key, defaultValue as Long) as T
            is Float -> sharedPrefs.getFloat(key, defaultValue as Float) as T
            is String -> sharedPrefs.getString(key, defaultValue as String) as T
            is Boolean -> sharedPrefs.getBoolean(key, defaultValue as Boolean) as T
            else -> error("This type $defaultValue can not be stored into Preferences")
        }
    }
}

class PrefLiveObjDelegate<T>(
    private val fieldKey: String,
    private val adapter: JsonAdapter<T>,
    private val preferences: SharedPreferences
) : ReadOnlyProperty<Any, LiveData<T?>> {
    private var storedValue: LiveData<T?>? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): LiveData<T?> {
        if (storedValue == null) {
            storedValue = SharedPreferenceObjLiveData(preferences, fieldKey, adapter)
        }
        return storedValue!!
    }
}

internal class SharedPreferenceObjLiveData<T>(
    var sharedPrefs: SharedPreferences,
    var key: String,
    val adapter: JsonAdapter<T>
) : LiveData<T?>() {
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, shKey ->
            if (shKey == key) {
                value = readValue()
            }
        }

    override fun onActive() {
        super.onActive()
        value = readValue()
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }

    @Suppress("UNCHECKED_CAST")
    private fun readValue(): T? {
        val string = sharedPrefs.getString(key, null)
        return string?.let { adapter.fromJson(it) }
    }
}