package com.idloquy.landmark.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.idloquy.landmark.ui.shared_marks.SharedMarkGroupAddMarksScreen
import com.idloquy.landmark.ui.shared_marks.SharedMarksScreen
import com.idloquy.landmark.ui.shared_marks.ViewSharedMarkGroupScreen
import com.idloquy.landmark.ui.shared_marks.import_marks.ImportSharedMarksScreen
import com.idloquy.landmark.ui.shared_marks.share_marks.ShareMarksDoneScreen
import com.idloquy.landmark.ui.shared_marks.share_marks.ShareMarksGroupNameScreen
import com.idloquy.landmark.ui.shared_marks.share_marks.ShareMarksSelectScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object History

@Serializable
data class Mark(val id: Int, val groupId: String? = null)

@Serializable
object SharedMarks

@Serializable
data class ViewSharedMarkGroup(
    val id: String,
)

@Serializable
data class SharedMarkGroupAddMarks(
    val groupId: String,
)

@Serializable
object ShareMarks

@Serializable
object ShareMarksGroupName

@Serializable
data class ShareMarksSelect(
    val groupName: String,
)

@Serializable
data class ShareMarksDone(
    val groupId: String,
)

@Serializable
object ShareMarksNavigateBack

@Serializable
object ImportSharedMarks

@Serializable
object ImportSharedMarksGroupId

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Home,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it } },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it } },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } },
    ) {
        composable<Home> {
            HomeScreen(
                onHistory = {
                    navController.navigate(History)
                })
        }
        composable<History> {
            HistoryScreen(
                onBack = {
                    navController.popBackStack()
                }, onSharedMarks = {
                    navController.navigate(SharedMarks)
                }, onViewMark = { id ->
                    navController.navigate(Mark(id))
                }
            )
        }
        composable<Mark> { backStackEntry ->
            val mark: Mark = backStackEntry.toRoute()
            MarkScreen(
                markId = mark.id,
                groupId = mark.groupId,
                onBack = {
                    navController.popBackStack()
                },
            )
        }
        composable<SharedMarks> {
            Log.d("landmark", "sharedmarks composed with backstack: ${navController.currentBackStack.value}")
            SharedMarksScreen(
                onBack = {
                    navController.popBackStack()
                },
                onViewGroup = {
                    navController.navigate(ViewSharedMarkGroup(it))
                },
                onShareMarks = {
                    navController.navigate(ShareMarks)
                },
                onImportMarks = {
                    navController.navigate(ImportSharedMarks)
                }
            )
        }
        composable<ViewSharedMarkGroup> { backStackEntry ->
            val params: ViewSharedMarkGroup = backStackEntry.toRoute()
            ViewSharedMarkGroupScreen(
                groupId = params.id,
                onBack = {
                    navController.popBackStack()
                },
                onAddMarks = {
                    navController.navigate(SharedMarkGroupAddMarks(params.id))
                },
                onViewMark = { id ->
                    navController.navigate(
                        Mark(
                            id = id,
                            groupId = params.id
                        )
                    )
                }
            )
        }
        composable<SharedMarkGroupAddMarks> { backStackEntry ->
            val params: SharedMarkGroupAddMarks = backStackEntry.toRoute()
            SharedMarkGroupAddMarksScreen(
                groupId = params.groupId,
                onBack = {
                    navController.popBackStack()
                },
                onDone = {
                    navController.popBackStack(
                        ViewSharedMarkGroup(params.groupId),
                        inclusive = false
                    )
                }
            )
        }
        navigation<ShareMarks>(startDestination = ShareMarksGroupName) {
            composable<ShareMarksGroupName> {
                ShareMarksGroupNameScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNext = {
                        navController.navigate(ShareMarksSelect(
                            groupName = it
                        ))
                    }
                )
            }
            composable<ShareMarksSelect> { backStackEntry ->
                val params: ShareMarksSelect = backStackEntry.toRoute()

                ShareMarksSelectScreen(
                    groupName = params.groupName,
                    onBack = {
                        navController.popBackStack()
                    },
                    onNext = { groupId ->
                        navController.navigate(ShareMarksDone(
                            groupId = groupId,
                        ))
                    },
                )
            }
            composable<ShareMarksDone> { backStackEntry ->
                val params: ShareMarksDone = backStackEntry.toRoute()

                ShareMarksDoneScreen(
                    groupId = params.groupId,
                    onBack = {
                        Log.d("landmark", "navigating to sharedmarks")
                        navController.popBackStack(SharedMarks, inclusive = false)
                    }
                )
            }
        }
        navigation<ImportSharedMarks>(startDestination = ImportSharedMarksGroupId) {
            composable<ImportSharedMarksGroupId> {
                ImportSharedMarksScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(ViewSharedMarkGroup(it)) {
                            popUpTo(SharedMarks)
                        }
                    },
                )
            }
        }
    }
}
