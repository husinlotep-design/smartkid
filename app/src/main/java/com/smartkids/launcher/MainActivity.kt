package com.smartkids.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartkids.launcher.presentation.AppSettingsScreen
import com.smartkids.launcher.presentation.JarvisScreen
import com.smartkids.launcher.presentation.LauncherScreen
import com.smartkids.launcher.presentation.PinScreen
import com.smartkids.launcher.service.ScreenTimeManager
import com.smartkids.launcher.service.ScreenTimeService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var screenTimeManager: ScreenTimeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start screen time service
        try {
            startForegroundService(
                Intent(this, ScreenTimeService::class.java).apply {
                    putExtra("limit_minutes", 60)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Block back button completely
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing — intentionally empty

                }
            }
        )

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "launcher",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable("launcher") {
                    LauncherScreen(
                        onOpenSettings = { navController.navigate("pin") },
                        onOpenJarvis = { navController.navigate("jarvis") }
                    )
                }

                composable("pin") {
                    PinScreen(
                        onSuccess = {
                            navController.navigate("settings") {
                                popUpTo("pin") { inclusive = true }
                            }
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }

                composable("settings") {
                    AppSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("jarvis") {
                    JarvisScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If SmartKids is no longer the default launcher

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val current = packageManager
            .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName

        if (current != packageName) {
            // Not default anymore — force the picker open
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}