package com.okaythis.jasvir

import android.app.Application
import com.itransition.protectoria.psa_multitenant.restapi.GatewayRestServer
import com.okaythis.jasvir.logger.OkayDemoLogger
import com.okaythis.fccabstractcore.interfaces.data.AbstractFccData
import com.okaythis.fccabstractcore.interfaces.data.AbstractFlutterEngineDependency
import com.okaythis.fccabstractcore.interfaces.fcc.FccApi
import com.okaythis.fccabstractcore.interfaces.parcel.Parcel
import com.okaythis.fluttercommunicationchannel.fcc.FccApiImpl
import com.protectoria.psa.PsaManager
import com.protectoria.psa.dex.common.data.json.PsaGsonFactory

class OkayDemoApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initPsa()
        initGatewayServer()
    }

    private fun initPsa() {
        val psaManager = PsaManager.init(this, OkayDemoLogger())
        psaManager.setPssAddress(BuildConfig.SERVER_URL)
        PsaManager.getInstance().setFccApi(FccApiImpl.INSTANCE as FccApi<Parcel, AbstractFccData, AbstractFlutterEngineDependency>)
        PsaManager.getInstance().setScreenshotEnabled(false)
    }

    private fun initGatewayServer() {
        GatewayRestServer.init(PsaGsonFactory().create(), BuildConfig.SERVER_URL + "/gateway/")
    }
}
