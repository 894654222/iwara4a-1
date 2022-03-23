package com.rerere.iwara4a.ui.activity

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLink
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import coil.compose.LocalImageLoader
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.rerere.iwara4a.ui.local.LocalNavController
import com.rerere.iwara4a.ui.local.LocalPipMode
import com.rerere.iwara4a.ui.local.LocalScreenOrientation
import com.rerere.iwara4a.ui.screen.about.AboutScreen
import com.rerere.iwara4a.ui.screen.chat.ChatScreen
import com.rerere.iwara4a.ui.screen.download.DownloadScreen
import com.rerere.iwara4a.ui.screen.follow.FollowScreen
import com.rerere.iwara4a.ui.screen.forum.ForumScreen
import com.rerere.iwara4a.ui.screen.friends.FriendsScreen
import com.rerere.iwara4a.ui.screen.history.HistoryScreen
import com.rerere.iwara4a.ui.screen.image.ImageScreen
import com.rerere.iwara4a.ui.screen.index.IndexScreen
import com.rerere.iwara4a.ui.screen.like.LikeScreen
import com.rerere.iwara4a.ui.screen.log.LoggerScreen
import com.rerere.iwara4a.ui.screen.login.LoginScreen
import com.rerere.iwara4a.ui.screen.message.MessageScreen
import com.rerere.iwara4a.ui.screen.playlist.PlaylistDialog
import com.rerere.iwara4a.ui.screen.search.SearchScreen
import com.rerere.iwara4a.ui.screen.self.SelfScreen
import com.rerere.iwara4a.ui.screen.setting.SettingScreen
import com.rerere.iwara4a.ui.screen.splash.SplashScreen
import com.rerere.iwara4a.ui.screen.user.UserScreen
import com.rerere.iwara4a.ui.screen.video.VideoScreen
import com.rerere.iwara4a.ui.theme.Iwara4aTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

@AndroidEntryPoint
class RouterActivity : ComponentActivity() {
    private val viewModel: ActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 全屏
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 初始化启动页面
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.checked
            }
        }

        setContent {
            val navController = rememberAnimatedNavController()

            LaunchedEffect(
                viewModel.checked,
                viewModel.cookieValid,
                viewModel.checkingCookkie
            ) {
                if (viewModel.checked && !viewModel.checkingCookkie) {
                    // 前往主页
                    if (viewModel.cookieValid) {
                        navController.navigate("index") {
                            popUpTo("splash") {
                                inclusive = true
                            }
                        }
                    } else {
                        // 登录
                        navController.navigate("login") {
                            popUpTo("splash") {
                                inclusive = true
                            }
                        }
                    }
                }
            }

            CompositionLocalProvider(
                LocalScreenOrientation provides viewModel.screenOrientation,
                LocalNavController provides navController,
                LocalPipMode provides viewModel.pipMode
            ) {
                Iwara4aTheme {
                    val systemUiController = rememberSystemUiController()
                    val dark = MaterialTheme.colors.isLight

                    // set ui color
                    SideEffect {
                        systemUiController.setNavigationBarColor(
                            Color.Transparent,
                            darkIcons = dark
                        )
                        systemUiController.setStatusBarColor(
                            Color.Transparent,
                            darkIcons = dark
                        )
                    }

                    AnimatedNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = "splash",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = {
                                    it
                                },
                                animationSpec = tween()
                            )
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = {
                                    -it
                                },
                                animationSpec = tween()
                            )
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = {
                                    -it
                                },
                                animationSpec = tween()
                            )
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = {
                                    it
                                },
                                animationSpec = tween()
                            )
                        }
                    ) {
                        composable(
                            route = "splash",
                            exitTransition = {
                                fadeOut()
                            }
                        ) {
                            // SplashScreen(navController)
                        }

                        composable(
                            route = "index",
                            enterTransition = {
                                fadeIn()
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = {
                                        -it
                                    },
                                    animationSpec = tween()
                                )
                            }
                        ) {
                            IndexScreen(navController)
                        }

                        composable("login") {
                            LoginScreen(navController)
                        }

                        composable("video/{videoId}",
                            arguments = listOf(
                                navArgument("videoId") {
                                    type = NavType.StringType
                                }
                            ),
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern = "https://ecchi.iwara.tv/videos/{videoId}"
                                }
                            )
                        ) {
                            VideoScreen(
                                navController,
                                it.arguments?.getString("videoId")!!
                            )
                        }

                        composable("image/{imageId}",
                            arguments = listOf(
                                navArgument("imageId") {
                                    type = NavType.StringType
                                }
                            ),
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern = "https://ecchi.iwara.tv/images/{imageId}"
                                }
                            )
                        ) {
                            ImageScreen(
                                navController,
                                it.arguments?.getString("imageId")!!
                            )
                        }

                        composable("user/{userId}",
                            arguments = listOf(
                                navArgument("userId") {
                                    type = NavType.StringType
                                }
                            ),
                            deepLinks = listOf(NavDeepLink("https://ecchi.iwara.tv/users/{userId}"))
                        ) {
                            UserScreen(
                                navController,
                                it.arguments?.getString("userId")!!
                            )
                        }

                        composable(
                            route = "search"
                        ) {
                            SearchScreen()
                        }

                        composable("about") {
                            AboutScreen()
                        }


                        composable("playlist?nid={nid}", arguments = listOf(
                            navArgument("nid") {
                                defaultValue = 0
                                type = NavType.IntType
                            }
                        )) {
                            PlaylistDialog(
                                navController,
                                it.arguments!!.getInt("nid"),
                                it.arguments!!.getString("playlist-id") ?: ""
                            )
                        }

                        composable("playlist?playlist-id={playlist-id}", arguments = listOf(
                            navArgument("playlist-id") {
                                defaultValue = ""
                                type = NavType.StringType
                            }
                        )) {
                            PlaylistDialog(
                                navController,
                                it.arguments!!.getInt("nid"),
                                it.arguments!!.getString("playlist-id") ?: ""
                            )
                        }

                        composable("like") {
                            LikeScreen(navController)
                        }

                        composable("download") {
                            DownloadScreen(navController)
                        }

                        composable("setting") {
                            SettingScreen(navController)
                        }

                        composable("history") {
                            HistoryScreen()
                        }

                        composable("logger") {
                            LoggerScreen()
                        }

                        composable("self") {
                            SelfScreen()
                        }

                        composable("forum") {
                            ForumScreen()
                        }

                        composable("chat") {
                            ChatScreen()
                        }

                        composable("friends") {
                            FriendsScreen()
                        }


                        composable("message") {
                            MessageScreen()
                        }

                        composable("following") {
                            FollowScreen()
                        }
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 禁止强制暗色模式，因为已经适配了夜间模式，所以不需要强制反色
            // 国产UI似乎必需这样做(isForceDarkAllowed = false)才能阻止反色，原生会自动识别
            val existingComposeView = window.decorView
                .findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0) as? ComposeView
            existingComposeView?.isForceDarkAllowed = false
        }

        // 是否允许屏幕捕捉
        lifecycleScope.launch {
            getSharedPreferences(
                packageName + "_preferences",
                MODE_PRIVATE
            ).getBoolean("setting.preventscreencaptcha", false).let {
                if (it) {
                    // Toast.makeText(this@MainActivity, "已开启隐私模式", Toast.LENGTH_SHORT).show()
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (viewModel.screenOrientation != newConfig.orientation) {
            viewModel.screenOrientation = newConfig.orientation
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        viewModel.pipMode = isInPictureInPictureMode
    }
}