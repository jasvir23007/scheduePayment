package com.okaythis.jasvir.fcm

enum class NotificationType(val code: Int) {
    WAKE_UP(10),
    AUTH_RESULT(20),
    UNDEFINED(0);

    companion object {
        fun creator(code: Int): NotificationType {
            for (type in values()) {
                if (type.code == code) {
                    return type
                }
            }
            return UNDEFINED
        }
    }

}