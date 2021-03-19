package com.coaker.newsaggregatorapp

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network

/**
 * This class continues to check whether the user has an internet connection throughout their use of
 * the app.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class NetworkCallbackMonitor(application: Application) {
    private val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * This method starts the connectivity manager callback which checks if the user is connected to
     * a network. The boolean variable isConnected changes when network connection changes occur.
     */
    fun start() {

        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Variables.isConnected = true
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Variables.isConnected = false
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Variables.isConnected = false
            }
        })
    }

    /**
     * This method stops the network callback and is called when the app is closed.
     */
    fun stop() {
        cm.unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
    }
}