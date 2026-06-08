package com.lifeos.modules.lifeos_financetracker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lifeos.core.LifeOSModule
import com.lifeos.modules.lifeos_financetracker.ui.FinanceViewModel
import com.lifeos.modules.lifeos_financetracker.ui.finance.*

class FinanceTrackerModule : LifeOSModule {
    override val id: String = "finance"
    override val name: String = "Finance"
    override val icon: ImageVector = Icons.Default.AccountBalance
    override val description: String = "Finance & net worth"
    override val version: String = "2.0"

    @Composable
    override fun Content(onNavigateBack: () -> Unit, initialId: Long?) {
        val moduleNavController = rememberNavController()
        val viewModel: FinanceViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = moduleNavController,
            startDestination = "list",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("list") {
                BackHandler(onBack = onNavigateBack)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Back to LifeOS")
                        }
                    }
                    FinanceDashboardScreen(
                        uiState = uiState,
                        onAddTransaction = { moduleNavController.navigate("create") },
                        onEditTransaction = { txId -> moduleNavController.navigate("edit/$txId") },
                        onSettingsClick = { moduleNavController.navigate("settings") },
                        onRecurringClick = { moduleNavController.navigate("recurring") },
                        onTrendsClick = { moduleNavController.navigate("trends") },
                        onAddAccount = { moduleNavController.navigate("account/create") },
                        onAccountClick = { accountId -> moduleNavController.navigate("account/detail/$accountId") },
                        onPreviousMonth = { viewModel.navigateToPreviousMonth() },
                        onNextMonth = { viewModel.navigateToNextMonth() },
                        onTodayClick = { viewModel.navigateToCurrentMonth() },
                        onSinkingFundsClick = { moduleNavController.navigate("sinking_funds") },
                        onPersonalCardClick = { moduleNavController.navigate("personal_card") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            composable("create") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                TransactionEditorScreen(
                    transaction = null,
                    categories = uiState.categories,
                    accounts = uiState.accounts,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, amount, type, catId, accountId, toAccountId, date, note, onComplete ->
                        viewModel.saveTransaction(id, amount, type, catId, accountId, toAccountId, date, note, onComplete)
                    },
                    onDelete = { moduleNavController.popBackStack() }
                )
            }

            composable(
                route = "edit/{txId}",
                arguments = listOf(navArgument("txId") { type = NavType.LongType })
            ) { backStackEntry ->
                val txId = backStackEntry.arguments?.getLong("txId")
                val tx = uiState.monthlyTransactions.find { it.id == txId }
                BackHandler(onBack = { moduleNavController.popBackStack() })
                TransactionEditorScreen(
                    transaction = tx,
                    categories = uiState.categories,
                    accounts = uiState.accounts,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, amount, type, catId, accountId, toAccountId, date, note, onComplete ->
                        viewModel.saveTransaction(id, amount, type, catId, accountId, toAccountId, date, note, onComplete)
                    },
                    onDelete = {
                        txId?.let {
                            viewModel.deleteTransaction(it) {
                                moduleNavController.popBackStack("list", inclusive = false)
                            }
                        }
                    }
                )
            }

            composable("account/create") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                AccountEditorScreen(
                    existing = null,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, name, type, colorHex, startingBalance, onComplete ->
                        viewModel.saveAccount(id, name, type, colorHex, startingBalance, onComplete)
                    },
                    onSaveMortgage = { accountId, balance, apr, months, payment, onComplete ->
                        viewModel.saveMortgageDetails(accountId, balance, apr, months, payment, onComplete)
                    },
                    onDelete = { id, onComplete ->
                        viewModel.softDeleteAccount(id, onComplete)
                    }
                )
            }

            composable(
                route = "account/edit/{accountId}",
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getLong("accountId")
                val awb = uiState.accounts.find { it.account.id == accountId }
                BackHandler(onBack = { moduleNavController.popBackStack() })
                AccountEditorScreen(
                    existing = awb,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, name, type, colorHex, startingBalance, onComplete ->
                        viewModel.saveAccount(id, name, type, colorHex, startingBalance, onComplete)
                    },
                    onSaveMortgage = { accId, balance, apr, months, payment, onComplete ->
                        viewModel.saveMortgageDetails(accId, balance, apr, months, payment, onComplete)
                    },
                    onDelete = { id, onComplete ->
                        viewModel.softDeleteAccount(id, onComplete)
                    }
                )
            }

            composable(
                route = "account/detail/{accountId}",
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getLong("accountId")
                val awb = uiState.accounts.find { it.account.id == accountId }
                BackHandler(onBack = { moduleNavController.popBackStack() })
                if (awb != null) {
                    AccountDetailScreen(
                        awb = awb,
                        transactions = uiState.monthlyTransactions.filter {
                            it.accountId == accountId || it.toAccountId == accountId
                        },
                        categories = uiState.categories,
                        accounts = uiState.accounts,
                        onNavigateBack = { moduleNavController.popBackStack() },
                        onEditAccount = { moduleNavController.navigate("account/edit/$accountId") },
                        onUpdateSnapshot = { balance, onComplete ->
                            viewModel.updateSnapshot(awb, balance, onComplete)
                        }
                    )
                }
            }

            composable("recurring") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                RecurringTransactionsScreen(
                    recurring = uiState.recurringTransactions,
                    categories = uiState.categories,
                    accounts = uiState.accounts,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onAddRecurring = { moduleNavController.navigate("recurring/create") },
                    onEditRecurring = { id -> moduleNavController.navigate("recurring/edit/$id") },
                    onDeleteRecurring = { id, onComplete -> viewModel.deleteRecurring(id, onComplete) },
                    onToggleRecurring = { id, isActive -> viewModel.toggleRecurring(id, isActive) }
                )
            }

            composable("recurring/create") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                RecurringEditorScreen(
                    existing = null,
                    categories = uiState.categories,
                    accounts = uiState.accounts,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, label, amount, type, catId, accountId, toAccountId, freq, dom, dow, nextDate, onComplete ->
                        viewModel.saveRecurring(id, label, amount, type, catId, accountId, toAccountId, freq, dom, dow, nextDate, onComplete)
                    },
                    onDelete = { id, onComplete -> viewModel.deleteRecurring(id, onComplete) }
                )
            }

            composable(
                route = "recurring/edit/{recurringId}",
                arguments = listOf(navArgument("recurringId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recurringId = backStackEntry.arguments?.getLong("recurringId")
                val item = uiState.recurringTransactions.find { it.id == recurringId }
                BackHandler(onBack = { moduleNavController.popBackStack() })
                RecurringEditorScreen(
                    existing = item,
                    categories = uiState.categories,
                    accounts = uiState.accounts,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, label, amount, type, catId, accountId, toAccountId, freq, dom, dow, nextDate, onComplete ->
                        viewModel.saveRecurring(id, label, amount, type, catId, accountId, toAccountId, freq, dom, dow, nextDate, onComplete)
                    },
                    onDelete = { id, onComplete -> viewModel.deleteRecurring(id, onComplete) }
                )
            }

            composable("trends") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                TrendsScreen(
                    uiState = uiState,
                    onNavigateBack = { moduleNavController.popBackStack() }
                )
            }

            composable("settings") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                FinanceSettingsScreen(
                    categories = uiState.categories,
                    budgets = uiState.budgets,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSaveCategory = { id, name, iconName, colorHex, onComplete ->
                        viewModel.saveCategory(id, name, iconName, colorHex, onComplete)
                    },
                    onDeleteCategory = { cat, onComplete ->
                        viewModel.deleteCategory(cat, onComplete)
                    },
                    onSetBudget = { catId, amount, onComplete ->
                        viewModel.setBudget(catId, amount, onComplete)
                    },
                    onRemoveBudget = { catId, onComplete ->
                        viewModel.removeBudget(catId, onComplete)
                    }
                )
            }

            composable("sinking_funds") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                SinkingFundsScreen(
                    funds = uiState.sinkingFunds,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onAddFund = { moduleNavController.navigate("sinking_funds/create") },
                    onEditFund = { id -> moduleNavController.navigate("sinking_funds/edit/$id") },
                    onLogContribution = { fundId, amount, date, onComplete ->
                        viewModel.logSinkingContribution(fundId, amount, date, onComplete)
                    },
                    onDeleteContribution = { id, onComplete ->
                        viewModel.deleteContribution(id, onComplete)
                    }
                )
            }

            composable("personal_card") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                PersonalCardScreen(
                    balance = uiState.personalCardBalance,
                    personalTransactions = uiState.personalCardTransactions,
                    payments = uiState.personalCardPayments,
                    categories = uiState.categories,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onAddPayment = { amountCents, note, date, onComplete ->
                        viewModel.addPersonalCardPayment(amountCents, note, date, onComplete)
                    },
                    onDeletePayment = { id, onComplete ->
                        viewModel.deletePersonalCardPayment(id, onComplete)
                    }
                )
            }

            composable("sinking_funds/create") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                SinkingFundEditorScreen(
                    existing = null,
                    accounts = uiState.accounts,
                    categories = uiState.categories,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, name, target, initial, date, color, accountId, catId, dayOfMonth, onComplete ->
                        viewModel.saveSinkingFund(id, name, target, initial, date, color, accountId, catId, dayOfMonth, onComplete)
                    },
                    onDelete = null
                )
            }

            composable(
                route = "sinking_funds/edit/{fundId}",
                arguments = listOf(navArgument("fundId") { type = NavType.LongType })
            ) { backStackEntry ->
                val fundId = backStackEntry.arguments?.getLong("fundId")
                val fund = uiState.sinkingFunds.find { it.fund.id == fundId }?.fund
                BackHandler(onBack = { moduleNavController.popBackStack() })
                SinkingFundEditorScreen(
                    existing = fund,
                    accounts = uiState.accounts,
                    categories = uiState.categories,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, name, target, initial, date, color, accountId, catId, dayOfMonth, onComplete ->
                        viewModel.saveSinkingFund(id, name, target, initial, date, color, accountId, catId, dayOfMonth, onComplete)
                    },
                    onDelete = { id, onComplete -> viewModel.deleteSinkingFund(id, onComplete) }
                )
            }
        }
    }
}
