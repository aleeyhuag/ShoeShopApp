package com.agkomputech.shoeshop.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agkomputech.shoeshop.ShoeShopApplication
import com.agkomputech.shoeshop.ui.addshoe.AddShoeScreen
import com.agkomputech.shoeshop.ui.addshoe.AddShoeViewModel
import com.agkomputech.shoeshop.ui.manage.EditShoeScreen
import com.agkomputech.shoeshop.ui.manage.EditShoeViewModel
import com.agkomputech.shoeshop.ui.manage.ManageShoesScreen
import com.agkomputech.shoeshop.ui.manage.ManageShoesViewModel
import com.agkomputech.shoeshop.ui.scan.ScanScreen
import com.agkomputech.shoeshop.ui.scan.ScanViewModel
import com.agkomputech.shoeshop.ui.theme.ShoeShopTheme

private object Routes {
    const val SCAN = "scan"
    const val ADD_SHOE = "add_shoe"
    const val MANAGE_SHOES = "manage_shoes"
    const val EDIT_SHOE = "edit_shoe/{shoeId}"
    fun editShoe(shoeId: Long) = "edit_shoe/$shoeId"
}

class MainActivity : ComponentActivity() {

    private val scanViewModel: ScanViewModel by viewModels { viewModelFactory { app -> ScanViewModel(app.repository) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoeShopTheme {
                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                    )
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted -> hasCameraPermission = granted }

                LaunchedEffect(Unit) {
                    if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
                }

                Scaffold { padding ->
                    if (!hasCameraPermission) {
                        Text(
                            "Camera permission is needed to scan and add shoes.",
                            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                        )
                    } else {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = Routes.SCAN,
                            modifier = Modifier.fillMaxSize().padding(padding)
                        ) {
                            composable(Routes.SCAN) {
                                ScanScreen(
                                    viewModel = scanViewModel,
                                    onAddNewShoe = { navController.navigate(Routes.ADD_SHOE) },
                                    onManageShoes = { navController.navigate(Routes.MANAGE_SHOES) }
                                )
                            }
                            composable(Routes.ADD_SHOE) {
                                val addShoeViewModel: AddShoeViewModel = viewModel {
                                    AddShoeViewModel((application as ShoeShopApplication).repository)
                                }
                                AddShoeScreen(
                                    viewModel = addShoeViewModel,
                                    onSaved = { navController.popBackStack() },
                                    onCancel = { navController.popBackStack() }
                                )
                            }
                            composable(Routes.MANAGE_SHOES) {
                                val manageViewModel: ManageShoesViewModel = viewModel {
                                    ManageShoesViewModel((application as ShoeShopApplication).repository)
                                }
                                ManageShoesScreen(
                                    viewModel = manageViewModel,
                                    onEditShoe = { shoeId -> navController.navigate(Routes.editShoe(shoeId)) },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = Routes.EDIT_SHOE,
                                arguments = listOf(navArgument("shoeId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val shoeId = backStackEntry.arguments?.getLong("shoeId") ?: return@composable
                                val editViewModel: EditShoeViewModel = viewModel {
                                    EditShoeViewModel((application as ShoeShopApplication).repository, shoeId)
                                }
                                EditShoeScreen(
                                    viewModel = editViewModel,
                                    onSaved = { navController.popBackStack() },
                                    onCancel = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Small helper so each screen's ViewModel gets the Application's shared repository. */
private fun <T : ViewModel> androidx.activity.ComponentActivity.viewModelFactory(
    create: (ShoeShopApplication) -> T
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <U : ViewModel> create(modelClass: Class<U>, extras: CreationExtras): U {
        return create(application as ShoeShopApplication) as U
    }
}
