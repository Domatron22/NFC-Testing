package c.domatron.nfctest



/* Author: Dominic Triano
 * Date: 4/1/2019
 * Language: Kotlin
 * Project: NFCTest
 * Description:
 *      Test for the tagGame application
 *
 */

import c.domatron.nfctest.R
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), Listener {

    private var mEtMessage: EditText? = null
    private var mBtWrite: Button? = null
    private var mBtRead: Button? = null

    private var mNfcWriteFragment: NFCWriteFragment? = null
    private var mNfcReadFragment: NFCReadFragment? = null

    private var isDialogDisplayed = false
    private var isWrite = false

    private var mNfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initNFC()
    }

    private fun initViews() {

        mEtMessage = findViewById(R.id.et_message) as EditText
        mBtWrite = findViewById(R.id.btn_write) as Button
        mBtRead = findViewById(R.id.btn_read) as Button

        mBtWrite!!.setOnClickListener { view -> showWriteFragment() }
        mBtRead!!.setOnClickListener { view -> showReadFragment() }
    }

    private fun initNFC() {

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    private fun showWriteFragment() {

        isWrite = true

        mNfcWriteFragment = fragmentManager.findFragmentByTag(NFCWriteFragment.TAG) as NFCWriteFragment

        if (mNfcWriteFragment == null) {

            mNfcWriteFragment = NFCWriteFragment.newInstance()
        }
        mNfcWriteFragment!!.show(fragmentManager, NFCWriteFragment.TAG)

    }

    private fun showReadFragment() {

        mNfcReadFragment = fragmentManager.findFragmentByTag(NFCReadFragment.TAG) as NFCReadFragment

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment.newInstance()
        }
        mNfcReadFragment!!.show(fragmentManager, NFCReadFragment.TAG)

    }

    override fun onDialogDisplayed() {

        isDialogDisplayed = true
    }

    override fun onDialogDismissed() {

        isDialogDisplayed = false
        isWrite = false
    }

    override fun onResume() {
        super.onResume()
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val techDetected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val nfcIntentFilter = arrayOf(techDetected, tagDetected, ndefDetected)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
        if (mNfcAdapter != null)
            mNfcAdapter!!.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null)

    }

    override fun onPause() {
        super.onPause()
        if (mNfcAdapter != null)
            mNfcAdapter!!.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

        Log.d(TAG, "onNewIntent: " + intent.action!!)

        if (tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show()
            val ndef = Ndef.get(tag)

            if (isDialogDisplayed) {

                if (isWrite) {

                    val messageToWrite = mEtMessage!!.text.toString()
                    mNfcWriteFragment = fragmentManager.findFragmentByTag(NFCWriteFragment.TAG) as NFCWriteFragment
                    mNfcWriteFragment!!.onNfcDetected(ndef, messageToWrite)

                } else {

                    mNfcReadFragment = fragmentManager.findFragmentByTag(NFCReadFragment.TAG) as NFCReadFragment
                    mNfcReadFragment!!.onNfcDetected(ndef)
                }
            }
        }
    }

    companion object {

        val TAG = MainActivity::class.java.simpleName
    }
}