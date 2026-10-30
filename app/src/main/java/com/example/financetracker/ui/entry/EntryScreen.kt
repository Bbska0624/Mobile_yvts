package com.example.financetracker.ui.entry

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.R
import com.example.financetracker.data.RecordType
import com.example.financetracker.data.dateToMillis
import com.example.financetracker.data.millisToDate
import com.example.financetracker.ui.AppViewModelProvider
import kotlinx.coroutines.launch

private const val MAX_AMOUNT_LENGTH = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState = viewModel.uiState

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.isEditMode) R.string.edit_record else R.string.add_record
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        EntryBody(
            uiState = uiState,
            onRecordDetailsChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    if (viewModel.saveRecord()) {
                        navigateBack()
                    }
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        )
    }
}

@Composable
private fun EntryBody(
    uiState: EntryUiState,
    onRecordDetailsChange: (RecordDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val details = uiState.recordDetails
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLandscape) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TypeSelector(details, onRecordDetailsChange)
                    NameField(details, onRecordDetailsChange)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AmountField(details, onRecordDetailsChange)
                    DateField(details, onRecordDetailsChange)
                }
            }
        } else {
            TypeSelector(details, onRecordDetailsChange)
            NameField(details, onRecordDetailsChange)
            AmountField(details, onRecordDetailsChange)
            DateField(details, onRecordDetailsChange)
        }
        Button(
            onClick = onSaveClick,
            enabled = uiState.isEntryValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    details: RecordDetails,
    onRecordDetailsChange: (RecordDetails) -> Unit
) {
    val types = listOf(RecordType.INCOME, RecordType.EXPENSE)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.record_type),
            style = MaterialTheme.typography.labelLarge
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            types.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = details.type == type,
                    onClick = { onRecordDetailsChange(details.copy(type = type)) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size)
                ) {
                    Text(
                        stringResource(
                            if (type == RecordType.INCOME) R.string.income else R.string.expense
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NameField(
    details: RecordDetails,
    onRecordDetailsChange: (RecordDetails) -> Unit
) {
    OutlinedTextField(
        value = details.name,
        onValueChange = { onRecordDetailsChange(details.copy(name = it)) },
        label = { Text(stringResource(R.string.record_name)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AmountField(
    details: RecordDetails,
    onRecordDetailsChange: (RecordDetails) -> Unit
) {
    OutlinedTextField(
        value = details.amount,
        onValueChange = { value ->
            val digits = value.filter { it.isDigit() }.take(MAX_AMOUNT_LENGTH)
            onRecordDetailsChange(details.copy(amount = digits))
        },
        label = { Text(stringResource(R.string.record_amount)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    details: RecordDetails,
    onRecordDetailsChange: (RecordDetails) -> Unit
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = details.date,
        onValueChange = { },
        readOnly = true,
        label = { Text(stringResource(R.string.record_date)) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = stringResource(R.string.choose_date)
                )
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateToMillis(details.date)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onRecordDetailsChange(details.copy(date = millisToDate(millis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
