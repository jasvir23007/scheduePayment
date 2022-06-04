package com.okaythis.jasvir.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.itransition.protectoria.psa_multitenant.protocol.scenarios.linking.LinkingScenarioListener
import com.itransition.protectoria.psa_multitenant.state.ApplicationState
import com.okaythis.jasvir.BuildConfig
import com.okaythis.jasvir.data.repository.PreferenceRepo
import com.okaythis.jasvir.databinding.ActivityMainBinding
import com.okaythis.jasvir.fcm.OkayDemoFirebaseMessagingService
import com.okaythis.jasvir.utils.PermissionHelper
import com.protectoria.psa.PsaManager
import com.protectoria.psa.api.PsaConstants
import com.protectoria.psa.api.converters.PsaIntentUtils
import com.protectoria.psa.api.entities.SpaAuthorizationData
import com.protectoria.psa.api.entities.SpaEnrollData
import com.protectoria.psa.api.entities.TenantInfoData
import com.protectoria.psa.dex.common.data.enums.PsaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val vm: MainViewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }
    private lateinit var preferenceRepo: PreferenceRepo
    private val permissionHelper = PermissionHelper(this)


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        preferenceRepo = PreferenceRepo(this)
        setContentView(binding.root)
        setClick()
        setObservers()
        checkPermissions()
        fetchInstanceId()
        handleIntent(intent)
    }

    private fun setClick() {
        binding.etTime.setOnClickListener {
            showTimeDialog()
        }
    }


    private fun setObservers() {
        vm.transactions.observe(this) {
            setUpRecyclerView(it)
        }
    }


    private fun setUpRecyclerView(list: ArrayList<String>) {
        binding.rvTransaction.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = PaymentAdapter(list)
        }
    }


    private fun showTimeDialog() {
        val mCurrentTime: Calendar = Calendar.getInstance()
        val hour: Int = mCurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute: Int = mCurrentTime.get(Calendar.MINUTE)
        val mTimePicker = TimePickerDialog(
            binding.etTime.context,
            { timePicker, selectedHour, selectedMinute -> binding.etTime.setText("$selectedHour:$selectedMinute") },
            hour,
            minute,
            true
        ) //Yes 24 hour time

        mTimePicker.setTitle("Select Time")
        mTimePicker.show()
    }



    private fun beginEnrollment() {
        val appPns = preferenceRepo.appPNS
        Toast.makeText(this@MainActivity, appPns + " " + BuildConfig.SERVER_URL + " " + BuildConfig.INSTALLATION_ID, Toast.LENGTH_LONG).show()
        val spaEnroll = SpaEnrollData(appPns,
            BuildConfig.PUB_PSS_B64,
            BuildConfig.INSTALLATION_ID,
            null,
            PsaType.OKAY)
        PsaManager.startEnrollmentActivity(this@MainActivity, spaEnroll)
    }

    private fun checkPermissions() {
        val requiredPermissions = PsaManager.getRequiredPermissions()
        if (!permissionHelper.hasPermissions(this, requiredPermissions)) {
            permissionHelper.requestPermissions(requiredPermissions)
        }
    }


    private fun linkUser(linkingCode: String) {
        val psaManager = PsaManager.getInstance()
        val linkingScenarioListener: LinkingScenarioListener = object: LinkingScenarioListener {
            override fun onLinkingCompletedSuccessful(var1: Long, var3: String){
                Toast.makeText(this@MainActivity, "Linking Successful", Toast.LENGTH_LONG).show()
            }

            override fun onLinkingFailed(var1: ApplicationState) {
              //  Toast.makeText(this@MainActivity, "Linking not Successful: linkingCode: ${linkingCodeEditText.text} errorCode: ${var1.code} ", Toast.LENGTH_LONG).show()
            }
        }

        psaManager.linkTenant(linkingCode, preferenceRepo, linkingScenarioListener)
    }

    private fun fetchInstanceId () {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("", "getInstanceId failed", task.exception)
                    Toast.makeText(this@MainActivity, "Error could not fetch token", Toast.LENGTH_LONG).show()
                    return@OnCompleteListener
                }
                val token = task.result?.token
                Log.i("token","=="+token)
                preferenceRepo.putAppPNS(token.toString())
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startAuthorization(sessionId: Long) {
        PsaManager.startAuthorizationActivity(this, SpaAuthorizationData(sessionId,
            preferenceRepo.appPNS,null,
           // BaseTheme(this).DEFAULT_PAGE_THEME,
            PsaType.OKAY, TenantInfoData("Okay AS", "")
        )
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleIntent(intent: Intent?) {
        intent?.apply {
            val sessionId =  getLongExtra(OkayDemoFirebaseMessagingService.ACTIVITY_WAKE_UP_KEY, 0)
            if (sessionId > 0)  {
                Toast.makeText(this@MainActivity, "Current sessionId $sessionId ", Toast.LENGTH_LONG).show()
                startAuthorization(sessionId)
            }
        }
    }

//    private fun startServerAuthorization(userExternalId: String?) {
//        transactionHandler.authorizeTransaction(userExternalId).enqueue(object:
//            Callback<AuthorizationResponse> {
//            override fun onFailure(call: Call<AuthorizationResponse>, t: Throwable) {
//                Toast.makeText(this@MainActivity, "Error making request to Server", Toast.LENGTH_LONG).show()
//            }
//
//            override fun onResponse(
//                call: Call<AuthorizationResponse>,
//                response: Response<AuthorizationResponse>
//            ) {
//                Toast.makeText(this@MainActivity, "Request made successfully ", Toast.LENGTH_LONG).show()
//            }
//
//        })
//    }
//
//    private fun startServerLinking(userExternalId: String?) {
//        transactionHandler.linkUser(userExternalId).enqueue(object: Callback<OkayLinking> {
//            override fun onFailure(call: Call<OkayLinking>, t: Throwable) {
//                Toast.makeText(this@MainActivity, "Error making request to Server ${t.localizedMessage}", Toast.LENGTH_LONG).show()
//                t.printStackTrace()
//            }
//
//            override fun onResponse(call: Call<OkayLinking>, response: Response<OkayLinking>) {
//                linkUser(response?.body()!!.linkingCode)
//            }
//
//        })
//
//    }
//
//    private fun startPinAuthorization(userExternalId: String?) {
//        transactionHandler.authorizePinTransaction(userExternalId).enqueue(object:
//            Callback<AuthorizationResponse> {
//            override fun onFailure(call: Call<AuthorizationResponse>, t: Throwable) {
//                Toast.makeText(this@MainActivity, "Error making request to Server", Toast.LENGTH_LONG).show()
//            }
//
//            override fun onResponse(
//                call: Call<AuthorizationResponse>,
//                response: Response<AuthorizationResponse>
//            ) {
//                Toast.makeText(this@MainActivity, "Request made successfully ", Toast.LENGTH_LONG).show()
//            }
//
//        })
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_ENROLL) {
            if (resultCode == RESULT_OK) {
                //We should save data from Enrollment result, for future usage
                data?.run {
                    val resultData = PsaIntentUtils.enrollResultFromIntent(this)
                    resultData.let {
                        preferenceRepo.putExternalId(it.externalId)
                    }
                    Toast.makeText(applicationContext,   "Successfully got this externalId " + resultData.externalId, Toast.LENGTH_SHORT).show()
                }
            } else {
              //  Toast.makeText(this, "Error Retrieving intent after enrollment:- code: ${linkingCodeEditText.text} errorCode: $resultCode", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_AUTHORIZATION) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Authorization granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Authorization not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }




}