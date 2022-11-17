package de.mm20.launcher2.ui.settings.searchactions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.searchactions.actions.SearchActionIcon
import de.mm20.launcher2.searchactions.builders.AppSearchActionBuilder
import de.mm20.launcher2.searchactions.builders.CustomizableSearchActionBuilder
import de.mm20.launcher2.searchactions.builders.WebsearchActionBuilder
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.BottomSheetDialog
import de.mm20.launcher2.ui.component.SearchActionIcon
import de.mm20.launcher2.ui.ktx.toPixels

@Composable
fun EditSearchActionSheet(
    initialSearchAction: CustomizableSearchActionBuilder?,
    onSave: (CustomizableSearchActionBuilder) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit = {},
) {
    val viewModel: EditSearchActionSheetVM = viewModel()
    LaunchedEffect(initialSearchAction) {
        viewModel.init(initialSearchAction)
    }
    val createNew by viewModel.createNew
    val page by viewModel.currentPage

    val searchAction by viewModel.searchAction
    BottomSheetDialog(
        onDismissRequest = {
            viewModel.onDismiss()
            onDismiss()
        },
        dismissOnBackPress = {
            page != EditSearchActionPage.PickIcon
        },
        swipeToDismiss = {
            page != EditSearchActionPage.PickIcon
        },
        confirmButton = when (page) {
            EditSearchActionPage.CustomizeAppSearch,
            EditSearchActionPage.CustomizeWebSearch,
            EditSearchActionPage.CustomizeCustomIntent -> {
                {
                    Button(onClick = {
                        if (viewModel.validate()) {
                            viewModel.onSave()
                            searchAction?.let { onSave(it) }
                        }
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }

            EditSearchActionPage.InitWebSearch -> {
                {
                    val density = LocalDensity.current
                    Button(
                        onClick = {
                            if (viewModel.skipWebsearchImport.value) {
                                viewModel.skipWebsearchImport()
                            } else {
                                viewModel.importWebsearch(density)
                            }
                        },
                        enabled = !viewModel.loadingWebsearch.value
                    ) {
                        Text(
                            stringResource(
                                if (viewModel.skipWebsearchImport.value) {
                                    R.string.skip
                                } else {
                                    R.string.action_continue
                                }
                            )
                        )
                    }
                }
            }

            EditSearchActionPage.PickIcon -> {
                {
                    OutlinedButton(onClick = {
                        viewModel.applyIcon()
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }

            else -> null
        },
        actions = {
            var showMenu by remember { mutableStateOf(false) }
            if (!createNew) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.menu_delete)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
                            },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        },
        title = {
            Text(
                stringResource(
                    if (createNew) {
                        R.string.create_search_action_title
                    } else {
                        R.string.edit_search_action_title
                    }
                )
            )
        }) {
        when (page) {
            EditSearchActionPage.SelectType -> SelectTypePage(viewModel)
            EditSearchActionPage.InitWebSearch -> InitWebSearchPage(viewModel)
            EditSearchActionPage.InitAppSearch -> InitAppSearchPage(viewModel)
            EditSearchActionPage.CustomizeWebSearch -> CustomizeWebSearch(viewModel)
            EditSearchActionPage.CustomizeCustomIntent -> CustomizeCustomIntent(viewModel)
            EditSearchActionPage.CustomizeAppSearch -> CustomizeAppSearch(viewModel)
            EditSearchActionPage.PickIcon -> PickIcon(viewModel)
        }
    }
}

@Composable
private fun SelectTypePage(viewModel: EditSearchActionSheetVM) {
    Column {
        Text(
            text = stringResource(R.string.create_search_action_type),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        OutlinedCard(
            onClick = { viewModel.initWebSearch() },
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.TravelExplore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.create_search_action_type_web),
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelLarge,

                    )
            }
        }
        OutlinedCard(
            onClick = { viewModel.initAppSearch() },
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ManageSearch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.create_search_action_type_app),
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        OutlinedCard(
            onClick = { viewModel.initCustomIntent() },
            modifier = Modifier
                .padding(top = 12.dp, bottom = 16.dp)
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.create_search_action_type_intent),
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun InitAppSearchPage(viewModel: EditSearchActionSheetVM) {
    val context = LocalContext.current
    val searchableApps by remember { viewModel.getSearchableApps(context) }.collectAsState(null)

    if (searchableApps != null) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.create_search_action_pick_app),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            items(searchableApps!!) {
                OutlinedCard(
                    onClick = { viewModel.selectSearchableApp(it) },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SearchActionIcon(
                            size = 24.dp,
                            componentName = it.componentName,
                            icon = de.mm20.launcher2.searchactions.actions.SearchActionIcon.Custom,
                            color = 1,
                        )
                        Text(
                            text = it.label,
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InitWebSearchPage(viewModel: EditSearchActionSheetVM) {
    var url by viewModel.initWebsearchUrl
    val importError by viewModel.websearchImportError
    val loading by viewModel.loadingWebsearch
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.create_search_action_website_url),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        val density = LocalDensity.current
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            value = url, onValueChange = { url = it },
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = { viewModel.importWebsearch(density) }),
            enabled = !loading,
            trailingIcon = {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        )
        if (importError) {
            Surface(
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.create_search_action_website_invalid_url),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CustomizeWebSearch(viewModel: EditSearchActionSheetVM) {
    val searchAction by viewModel.searchAction

    Column {

        if (searchAction != null && searchAction is WebsearchActionBuilder) {
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                SearchActionIconTile(onClick = {
                    viewModel.openIconPicker()
                }) {
                    SearchActionIcon(
                        builder = searchAction!!, size = 24.dp
                    )
                }
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    value = searchAction!!.label,
                    onValueChange = { viewModel.setLabel(it) },
                    label = { Text(stringResource(R.string.search_action_label)) },
                )
            }

            val placeholderBackground = MaterialTheme.colorScheme.tertiary
            val placeholderColor = MaterialTheme.colorScheme.onTertiary
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                value = (searchAction as WebsearchActionBuilder).urlTemplate,
                onValueChange = { viewModel.setUrlTemplate(it) },
                label = { Text(stringResource(R.string.search_action_websearch_url)) },
                supportingText = {
                    if (viewModel.websearchInvalidUrlError.value) {
                        Text(stringResource(R.string.websearch_dialog_url_error))
                    } else {
                        Column {
                            Text(stringResource(R.string.search_action_websearch_url_hint))
                            /** TODO: Write user guide for this and link it here
                            Text(
                            stringResource(R.string.more_information),
                            modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { },
                            color = MaterialTheme.colorScheme.secondary,
                            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline)
                            )
                             */
                        }
                    }
                },
                isError = viewModel.websearchInvalidUrlError.value,
                visualTransformation = {
                    TransformedText(buildAnnotatedString {
                        append(it)
                        val placeholderIndex = it.indexOf("\${1}")
                        if (placeholderIndex != -1) {
                            addStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = placeholderColor,
                                    background = placeholderBackground,
                                ),
                                placeholderIndex, placeholderIndex + 4
                            )
                        }
                    }, OffsetMapping.Identity)
                }
            )
        }
    }
}

@Composable
fun CustomizeAppSearch(viewModel: EditSearchActionSheetVM) {
    val searchAction by viewModel.searchAction
    val context = LocalContext.current

    val availableSearchApps by remember { viewModel.getSearchableApps(context) }.collectAsState(
        initial = emptyList()
    )
    val selectedApp =
        remember(availableSearchApps, (searchAction as? AppSearchActionBuilder)?.componentName) {
            availableSearchApps.find { it.componentName == (searchAction as? AppSearchActionBuilder)?.componentName }
        }

    Column {

        if (searchAction != null) {

            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                SearchActionIconTile(onClick = {
                    viewModel.openIconPicker()
                }) {
                    SearchActionIcon(
                        builder = searchAction!!, size = 24.dp
                    )
                }
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    value = searchAction!!.label,
                    onValueChange = { viewModel.setLabel(it) },
                    label = { Text(stringResource(R.string.search_action_label)) },
                )
            }

            var showAppDropdown by remember { mutableStateOf(false) }
            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable { showAppDropdown = !showAppDropdown }) {
                TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = selectedApp?.label ?: "",
                    enabled = true,
                    label = { Text(stringResource(R.string.search_action_app)) },
                    innerTextField = {
                        Text(
                            selectedApp?.label ?: "",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    interactionSource = remember { MutableInteractionSource() },
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    leadingIcon = {
                        if (selectedApp != null) {
                            SearchActionIcon(
                                size = 24.dp,
                                componentName = selectedApp.componentName,
                                icon = de.mm20.launcher2.searchactions.actions.SearchActionIcon.Custom,
                                color = 1,
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null)
                    }
                )
                DropdownMenu(
                    expanded = showAppDropdown,
                    onDismissRequest = { showAppDropdown = false }) {
                    for (app in availableSearchApps) {
                        DropdownMenuItem(
                            text = { Text(app.label) },
                            onClick = {
                                viewModel.setComponentName(app.componentName)
                                showAppDropdown = false
                            },
                            leadingIcon = {
                                SearchActionIcon(
                                    size = 24.dp,
                                    componentName = app.componentName,
                                    icon = de.mm20.launcher2.searchactions.actions.SearchActionIcon.Custom,
                                    color = 1,
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomizeCustomIntent(viewModel: EditSearchActionSheetVM) {
    val searchAction by viewModel.searchAction

    if (searchAction != null) {
        Column {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                SearchActionIconTile(onClick = {
                    viewModel.openIconPicker()
                }) {
                    SearchActionIcon(
                        builder = searchAction!!, size = 24.dp
                    )
                }
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    value = searchAction!!.label,
                    onValueChange = { viewModel.setLabel(it) },
                    label = { Text(stringResource(R.string.search_action_label)) },
                )
            }
        }
    }
}

@Composable
fun PickIcon(viewModel: EditSearchActionSheetVM) {
    val action by viewModel.searchAction

    val iconSizePx = 20.dp.toPixels()

    val pickIconLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            if (it != null) viewModel.importIcon(it, iconSizePx.toInt())
        }

    if (action?.customIcon == null) {

        val availableIcons =
            remember { SearchActionIcon.values().filter { it != SearchActionIcon.Custom } }

        Column {
            LazyVerticalGrid(columns = GridCells.Adaptive(64.dp)) {
                if (action is AppSearchActionBuilder) {
                    item {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val isSelected =
                                action?.icon == SearchActionIcon.Custom && action?.customIcon == null
                            SearchActionIconTile(isSelected, onClick = {
                                viewModel.setCustomIcon(null)
                            }) {
                                SearchActionIcon(
                                    icon = SearchActionIcon.Custom,
                                    componentName = (action as AppSearchActionBuilder).componentName,
                                    size = 24.dp,
                                    color = 1,
                                )
                            }
                        }
                    }
                }
                items(availableIcons) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val isSelected = action?.icon == it
                        SearchActionIconTile(isSelected, onClick = {
                            viewModel.setIcon(it)
                        }) {
                            SearchActionIcon(
                                icon = it,
                                size = 24.dp,
                                color = 0,
                            )
                        }
                    }
                }
            }
            TextButton(
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
                onClick = { pickIconLauncher.launch("image/*") }) {
                Text(stringResource(R.string.websearch_dialog_custom_icon))
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchActionIconTile {
                SearchActionIcon(builder = action!!, size = 24.dp)
            }
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(end = 16.dp),
                    text = "Monochrome",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelMedium,
                )
                Switch(
                    checked = action?.iconColor == 0,
                    onCheckedChange = { viewModel.setIconColor(if (it) 0 else 1) })
            }
            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                OutlinedButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = { pickIconLauncher.launch("image/*") }) {
                    Text(stringResource(R.string.websearch_dialog_replace_icon))
                }
                OutlinedButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = { viewModel.setIcon(SearchActionIcon.Search) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.websearch_dialog_delete_icon))
                }
            }
        }
    }
}

@Composable
private fun SearchActionIconTile(
    filled: Boolean = true,
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit,
) {

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .then(
                if (filled) Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                else Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.medium
                )
            )
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            icon()
        }
    }
}