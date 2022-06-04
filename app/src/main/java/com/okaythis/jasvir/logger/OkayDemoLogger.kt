package com.okaythis.jasvir.logger

import android.util.Log
import com.protectoria.psa.dex.common.utils.logger.ExceptionLogger
import java.lang.Exception

class OkayDemoLogger: ExceptionLogger {
    override fun setUserIdentificator(p0: String?) {
        Log.e("SET ID-: ", "Successfully set user identificator $p0 ")
    }

    override fun exception(p0: String?, p1: Exception?) {
        Log.e("Exception-: ", "Okay Error $p0 -- Exception: $p1")
    }


}