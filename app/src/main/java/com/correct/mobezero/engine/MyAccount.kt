package com.correct.mobezero.engine


import org.pjsip.pjsua2.Account
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.BuddyConfig
import org.pjsip.pjsua2.OnIncomingCallParam
import org.pjsip.pjsua2.OnInstantMessageParam
import org.pjsip.pjsua2.OnRegStateParam

class MyAccount internal constructor(config: AccountConfig) : Account() {

    public var buddyList: ArrayList<MyBuddy> = arrayListOf()
    public var cfg = AccountConfig()

    fun addBuddy(bud_cfg: BuddyConfig): MyBuddy? {
        /* Create Buddy */
        var bud: MyBuddy? = MyBuddy(bud_cfg)
        try {
            bud!!.create(this, bud_cfg)
        } catch (e: Exception) {
            bud!!.delete()
            bud = null
        }

        if (bud != null) {
            buddyList.add(bud)
            if (bud_cfg.subscribe) try {
                bud.subscribePresence(true)
            } catch (e: Exception) {
            }
        }

        return bud
    }

    fun delBuddy(buddy: MyBuddy) {
        buddyList.remove(buddy)
        buddy.delete()
    }

    fun delBuddy(index: Int) {
        val bud = buddyList[index]
        buddyList.removeAt(index)
        bud.delete()
    }

    override fun onRegState(prm: OnRegStateParam) {
        MyApp.observer.notifyRegState(
            prm.getCode(), prm.getReason(),
            prm.getExpiration()
        )
    }

    override fun onIncomingCall(prm: OnIncomingCallParam) {
        println("======== Incoming call ======== ")
        val call = MyCall(this, prm.getCallId())
        MyApp.observer.notifyIncomingCall(call)
    }

    // receive sms message with pjsip
    override fun onInstantMessage(prm: OnInstantMessageParam) {
        println("======== Incoming pager ======== ")
        System.out.println("From     : " + prm.getFromUri())
        System.out.println("To       : " + prm.getToUri())
        System.out.println("Contact  : " + prm.getContactUri())
        System.out.println("Mimetype : " + prm.getContentType())
        System.out.println("Body     : " + prm.getMsgBody())
    }
}