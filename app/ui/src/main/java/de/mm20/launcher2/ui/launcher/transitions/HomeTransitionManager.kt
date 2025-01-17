package de.mm20.launcher2.ui.launcher.transitions

import android.view.Window
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toAndroidRectF
import com.android.launcher3.GestureNavContract
import kotlinx.coroutines.flow.MutableSharedFlow

class HomeTransitionManager {

    val currentTransition = MutableSharedFlow<HomeTransition?>(1)

    private val handlers = mutableSetOf<HomeTransitionHandler>()

    fun resolve(gestureNavContract: GestureNavContract, window: Window) {
        for (handler in handlers) {
            val result = handler.handle(gestureNavContract)
            if (result != null) {
                val startRect = Rect(Offset(0f, 0f), Size(window.decorView.width.toFloat(), window.decorView.height.toFloat()))
                val targetBounds = result.targetBounds
                gestureNavContract.sendEndPosition(targetBounds.toAndroidRectF())
                currentTransition.tryEmit(HomeTransition(
                    startBounds = startRect,
                    icon = result.icon,
                    targetBounds = targetBounds,
                ))
                return
            }
        }
        currentTransition.tryEmit(null)
    }

    fun clear() {
        currentTransition.tryEmit(null)
    }

    fun registerHandler(handler: HomeTransitionHandler) {
        handlers.add(handler)
    }

    fun unregisterHandler(handler: HomeTransitionHandler) {
        handlers.remove(handler)
    }
}

val LocalHomeTransitionManager = compositionLocalOf<HomeTransitionManager?> { null }