package com.okaythis.jasvir.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.itransition.protectoria.psa_multitenant.protocol.scenarios.linking.LinkingScenarioListener
import com.itransition.protectoria.psa_multitenant.state.ApplicationState
import com.okaythis.jasvir.BuildConfig
import com.okaythis.jasvir.broadcast.AlarmReceiver
import com.okaythis.jasvir.data.repository.PreferenceRepo
import com.okaythis.jasvir.databinding.ActivityMainBinding
import com.okaythis.jasvir.fcm.OkayDemoFirebaseMessagingService
import com.okaythis.jasvir.utils.Constants.DESCRIPTION
import com.okaythis.jasvir.utils.PermissionHelper
import com.protectoria.psa.PsaManager
import com.protectoria.psa.api.PsaConstants
import com.protectoria.psa.api.converters.PsaIntentUtils
import com.protectoria.psa.api.entities.SpaAuthorizationData
import com.protectoria.psa.api.entities.SpaEnrollData
import com.protectoria.psa.api.entities.TenantInfoData
import com.protectoria.psa.dex.common.data.enums.PsaType
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val vm by viewModels<MainViewModel>()
    private lateinit var preferenceRepo: PreferenceRepo
    private val permissionHelper = PermissionHelper(this)
    private var description: String? = null
    private val scheduledTime = Calendar.getInstance()
    private val df = SimpleDateFormat("HH:mm", Locale.ENGLISH)

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

        binding.btSchedule.setOnClickListener {
           if(binding.etTime.text.toString().isEmpty()){
               Toast.makeText(
                   this@MainActivity,
                   "Please enter Time!",
                   Toast.LENGTH_LONG
               ).show()

               return@setOnClickListener
           }

            if(binding.etAmount.text.toString().isEmpty()){
                Toast.makeText(
                    this@MainActivity,
                    "Please enter amount!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            scheduleNotification()
        }
    }


    private fun setObservers() {
        vm.transactions.observe(this) {
            setUpRecyclerView(it)
        }
        vm.linkingCode.observe(this) {
            if (it != null)
                linkUser(it)
            else
                linkUser("123456")
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
            { timePicker, selectedHour, selectedMinute ->
                scheduledTime.timeInMillis = System.currentTimeMillis()
                scheduledTime.set(Calendar.HOUR_OF_DAY,selectedHour)
                scheduledTime.set(Calendar.MINUTE,selectedMinute)
                binding.etTime.setText(df.format(scheduledTime.timeInMillis))
            },
            hour,
            minute,
            true
        )
        mTimePicker.setTitle("Select Time")
        mTimePicker.show()
    }


    private fun beginEnrollment() {
        val appPns = preferenceRepo.appPNS
        Toast.makeText(
            this@MainActivity,
            appPns + " " + BuildConfig.SERVER_URL + " " + BuildConfig.INSTALLATION_ID,
            Toast.LENGTH_LONG
        ).show()
        val spaEnroll = SpaEnrollData(
            appPns,
            BuildConfig.PUB_PSS_B64,
            BuildConfig.INSTALLATION_ID,
            null,
            PsaType.OKAY
        )
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
        val linkingScenarioListener: LinkingScenarioListener = object : LinkingScenarioListener {
            override fun onLinkingCompletedSuccessful(var1: Long, var3: String) {
                Toast.makeText(this@MainActivity, "Linking Successful", Toast.LENGTH_LONG).show()
                if (description != null) {
                    vm.updateListTransaction(description!!)
                    description = null
                }
            }

            override fun onLinkingFailed(var1: ApplicationState) {
                Toast.makeText(this@MainActivity, "Linking Successful", Toast.LENGTH_LONG).show()
                if (description != null) {
                    vm.updateListTransaction(description!!)
                    description = null
                }
            }
        }

        psaManager.linkTenant(linkingCode, preferenceRepo, linkingScenarioListener)
    }

    private fun fetchInstanceId() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error could not fetch token",
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnCompleteListener
                }
                val token = task.result?.token
                preferenceRepo.putAppPNS(token.toString())
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startAuthorization(sessionId: Long) {
        PsaManager.startAuthorizationActivity(
            this, SpaAuthorizationData(
                sessionId,
                preferenceRepo.appPNS, null,
                PsaType.OKAY, TenantInfoData("Okay AS", "")
            )
        )
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleIntent(intent: Intent?) {
        intent?.apply {
            if (getStringExtra(DESCRIPTION) != null) {
                description = getStringExtra(DESCRIPTION)
                beginEnrollment()
            }
            val sessionId = getLongExtra(OkayDemoFirebaseMessagingService.ACTIVITY_WAKE_UP_KEY, 0)
            if (sessionId > 0) {
                Toast.makeText(
                    this@MainActivity,
                    "Current sessionId $sessionId ",
                    Toast.LENGTH_LONG
                ).show()
                startAuthorization(sessionId)
            }
        }
    }


    private fun scheduleNotification() {
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        alarmIntent.putExtra("amount", binding.etAmount.text.toString())
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            999,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


        val now = Calendar.getInstance()
        now.timeInMillis = System.currentTimeMillis()
        val schedulingTime = scheduledTime.timeInMillis - now.timeInMillis
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as? AlarmManager?
        val timeSchedule = now.timeInMillis + (schedulingTime/2)
        alarmMgr?.set(
            AlarmManager.RTC_WAKEUP,
            timeSchedule,
            pendingIntent
        )

        Toast.makeText(this@MainActivity, "Payment Scheduled at : " + df.format(timeSchedule), Toast.LENGTH_LONG).show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_ENROLL) {
            if (resultCode == RESULT_OK) {
                data?.run {
                    val resultData = PsaIntentUtils.enrollResultFromIntent(this)
                    resultData.let {
                        preferenceRepo.putExternalId(it.externalId)
                    }
                    Toast.makeText(
                        applicationContext,
                        "Successfully got this externalId " + resultData.externalId,
                        Toast.LENGTH_SHORT
                    ).show()
                    vm.startServerLinking("w1231f")
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