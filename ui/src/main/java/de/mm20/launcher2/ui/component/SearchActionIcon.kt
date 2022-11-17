package de.mm20.launcher2.ui.component

import android.content.ComponentName
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.mm20.launcher2.searchactions.actions.AppSearchAction
import de.mm20.launcher2.searchactions.actions.SearchAction
import de.mm20.launcher2.searchactions.actions.SearchActionIcon
import de.mm20.launcher2.searchactions.builders.AppSearchActionBuilder
import de.mm20.launcher2.searchactions.builders.CustomizableSearchActionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SearchActionIcon(
    icon: SearchActionIcon,
    color: Int = 0,
    customIcon: String? = null,
    componentName: ComponentName? = null,
    size: Dp = 20.dp
) {
    val tint = when(color) {
        0 -> MaterialTheme.colorScheme.primary
        1 -> Color.Unspecified
        else -> Color(color)
    }
    if (icon != SearchActionIcon.Custom || customIcon == null && componentName == null) {
        Icon(
            modifier = Modifier.size(size),
            imageVector = getSearchActionIconVector(icon),
            contentDescription = null,
            tint = tint,
        )
    } else if (customIcon == null && componentName != null) {
        val context = LocalContext.current
        var drawable by remember(componentName) { mutableStateOf<Drawable?>(null) }

        LaunchedEffect(componentName) {
            drawable = withContext(Dispatchers.IO) {
                context.packageManager.getActivityIcon(componentName)
            }
        }

        AsyncImage(
            model = drawable,
            contentDescription = null,
            modifier = Modifier.size(size),
            colorFilter = if (tint.isSpecified) ColorFilter.tint(tint) else null
        )
    } else {
        AsyncImage(
            model = customIcon,
            contentDescription = null,
            modifier = Modifier.size(size),
            colorFilter = if (tint.isSpecified) ColorFilter.tint(tint) else null
        )
    }
}

@Composable
fun SearchActionIcon(action: SearchAction, size: Dp = 20.dp) {
    SearchActionIcon(
        icon = action.icon,
        color = action.iconColor,
        customIcon = action.customIcon,
        componentName = (action as? AppSearchAction)?.componentName,
        size = size,
    )
}

@Composable
fun SearchActionIcon(builder: CustomizableSearchActionBuilder, size: Dp = 20.dp) {
   SearchActionIcon(
       icon = builder.icon,
       color = builder.iconColor,
       customIcon = builder.customIcon,
       componentName = (builder as? AppSearchActionBuilder)?.componentName,
       size = size,
   )
}

fun getSearchActionIconVector(icon: SearchActionIcon): ImageVector {
    return when (icon) {
        SearchActionIcon.Phone -> Icons.Rounded.Call
        SearchActionIcon.Website -> Icons.Rounded.Language
        SearchActionIcon.Alarm -> Icons.Rounded.Alarm
        SearchActionIcon.Timer -> Icons.Rounded.Timer
        SearchActionIcon.Contact -> Icons.Rounded.PersonAdd
        SearchActionIcon.Email -> Icons.Rounded.Email
        SearchActionIcon.Message -> Icons.Rounded.Sms
        SearchActionIcon.Calendar -> Icons.Rounded.Event
        SearchActionIcon.Translate -> Icons.Rounded.Translate
        else -> Icons.Rounded.Search
    }
}