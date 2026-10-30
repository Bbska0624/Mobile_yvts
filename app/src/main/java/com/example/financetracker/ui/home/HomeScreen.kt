package com.example.financetracker.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.R
import com.example.financetracker.data.FinanceRecord
import com.example.financetracker.data.RecordFilter
import com.example.financetracker.data.RecordType
import com.example.financetracker.data.formatAmount
import com.example.financetracker.ui.AppViewModelProvider
import com.example.financetracker.ui.theme.CategoryBlue
import com.example.financetracker.ui.theme.CategoryGreen
import com.example.financetracker.ui.theme.CategoryOrange
import com.example.financetracker.ui.theme.CategoryPink
import com.example.financetracker.ui.theme.ExpenseColor
import com.example.financetracker.ui.theme.ExpenseLightColor
import com.example.financetracker.ui.theme.IncomeColor
import com.example.financetracker.ui.theme.IncomeLightColor

private val SmallButtonPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToAddRecord: (RecordType) -> Unit,
    navigateToEditRecord: (Int) -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var recordIdToDelete by rememberSaveable { mutableStateOf<Int?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.menu)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                onClick = {
                                    menuExpanded = false
                                    navigateToSettings()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        HomeBody(
            uiState = uiState,
            onPreviousMonthClick = viewModel::showPreviousMonth,
            onNextMonthClick = viewModel::showNextMonth,
            onTotalsClick = viewModel::toggleAmountVisibility,
            onAmountClick = viewModel::toggleAmountVisibility,
            onAddClick = navigateToAddRecord,
            onEditClick = navigateToEditRecord,
            onDeleteClick = { recordIdToDelete = it },
            modifier = Modifier.padding(innerPadding)
        )
    }

    val recordToDelete = uiState.records.firstOrNull { it.id == recordIdToDelete }
    if (recordToDelete != null) {
        DeleteConfirmationDialog(
            onDeleteConfirm = {
                recordIdToDelete = null
                viewModel.deleteRecord(recordToDelete)
            },
            onDeleteCancel = { recordIdToDelete = null }
        )
    }
}

@Composable
private fun HomeBody(
    uiState: HomeUiState,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onTotalsClick: () -> Unit,
    onAmountClick: () -> Unit,
    onAddClick: (RecordType) -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonthBar(
                    uiState = uiState,
                    onPreviousClick = onPreviousMonthClick,
                    onNextClick = onNextMonthClick
                )
                SummaryRow(uiState = uiState, onTotalsClick = onTotalsClick)
                AddButtons(onAddClick = onAddClick)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                RecordList(
                    uiState = uiState,
                    onAmountClick = onAmountClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonthBar(
                uiState = uiState,
                onPreviousClick = onPreviousMonthClick,
                onNextClick = onNextMonthClick
            )
            SummaryRow(uiState = uiState, onTotalsClick = onTotalsClick)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                RecordList(
                    uiState = uiState,
                    onAmountClick = onAmountClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AddButtons(onAddClick = onAddClick)
        }
    }
}

@Composable
private fun MonthBar(
    uiState: HomeUiState,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousClick,
                enabled = uiState.hasAnyRecords
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_month)
                )
            }
            Text(
                text = uiState.selectedMonth.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onNextClick,
                enabled = uiState.hasAnyRecords
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_month)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    uiState: HomeUiState,
    onTotalsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            label = stringResource(R.string.income),
            amount = uiState.incomeTotal,
            isAmountVisible = uiState.areAmountsVisible,
            icon = Icons.Filled.ArrowUpward,
            color = IncomeColor,
            containerColor = IncomeLightColor,
            onClick = onTotalsClick,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = stringResource(R.string.expense),
            amount = uiState.expenseTotal,
            isAmountVisible = uiState.areAmountsVisible,
            icon = Icons.Filled.ArrowDownward,
            color = ExpenseColor,
            containerColor = ExpenseLightColor,
            onClick = onTotalsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SummaryCard(
    label: String,
    amount: Long,
    isAmountVisible: Boolean,
    icon: ImageVector,
    color: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIcon(icon = icon, color = color, size = 36)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
                Text(
                    text = if (isAmountVisible) {
                        formatAmount(amount)
                    } else {
                        stringResource(R.string.hidden_amount)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecordList(
    uiState: HomeUiState,
    onAmountClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (uiState.records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        when {
                            !uiState.hasAnyRecords -> R.string.no_records
                            uiState.filter == RecordFilter.ALL -> R.string.no_month_records
                            else -> R.string.no_filtered_records
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(uiState.records, key = { _, record -> record.id }) { index, record ->
                    RecordRow(
                        record = record,
                        isAmountVisible = uiState.areAmountsVisible,
                        onAmountClick = onAmountClick,
                        onEditClick = { onEditClick(record.id) },
                        onDeleteClick = { onDeleteClick(record.id) }
                    )
                    if (index < uiState.records.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordRow(
    record: FinanceRecord,
    isAmountVisible: Boolean,
    onAmountClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = record.type == RecordType.INCOME
    val typeColor = if (isIncome) IncomeColor else ExpenseColor
    val (icon, iconColor) = recordIcon(record)
    var rowMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { rowMenuExpanded = true },
                    onLongClick = onEditClick
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIcon(icon = icon, color = iconColor, size = 44)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = record.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isAmountVisible) {
                        (if (isIncome) "+ " else "- ") + formatAmount(record.amount)
                    } else {
                        stringResource(R.string.hidden_amount)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = onAmountClick,
                            onLongClick = onEditClick
                        )
                        .padding(4.dp)
                )
                Text(
                    text = record.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        DropdownMenu(
            expanded = rowMenuExpanded,
            onDismissRequest = { rowMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = {
                    rowMenuExpanded = false
                    onEditClick()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete), color = ExpenseColor) },
                leadingIcon = {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = ExpenseColor)
                },
                onClick = {
                    rowMenuExpanded = false
                    onDeleteClick()
                }
            )
        }
    }
}

private fun recordIcon(record: FinanceRecord): Pair<ImageVector, Color> {
    val name = record.name.lowercase()
    return when {
        "хоол" in name || "кафе" in name || "ресторан" in name ->
            Icons.Filled.Restaurant to CategoryOrange
        "унаа" in name || "тээвэр" in name || "такси" in name || "автобус" in name ->
            Icons.Filled.DirectionsBus to CategoryBlue
        "цалин" in name || "ажил" in name ->
            Icons.Filled.Work to CategoryGreen
        "дэлгүүр" in name || "хувцас" in name ->
            Icons.Filled.ShoppingBag to CategoryPink
        record.type == RecordType.INCOME -> Icons.Filled.Payments to CategoryGreen
        else -> Icons.Filled.ShoppingCart to CategoryPink
    }
}

@Composable
private fun CircleIcon(
    icon: ImageVector,
    color: Color,
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(color = color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size / 2).dp)
        )
    }
}

@Composable
private fun AddButtons(
    onAddClick: (RecordType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AddButton(
            text = stringResource(R.string.add_income),
            color = IncomeColor,
            onClick = { onAddClick(RecordType.INCOME) },
            modifier = Modifier.weight(1f)
        )
        AddButton(
            text = stringResource(R.string.add_expense),
            color = ExpenseColor,
            onClick = { onAddClick(RecordType.EXPENSE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AddButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = SmallButtonPadding,
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDeleteCancel,
        title = { Text(stringResource(R.string.delete_dialog_title)) },
        text = { Text(stringResource(R.string.delete_dialog_text)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(text = stringResource(R.string.yes), color = ExpenseColor)
            }
        }
    )
}
