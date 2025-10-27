package sp.sample.clicks

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sp.ax.clicks.onClick

internal class MainActivity : ComponentActivity() {
    private class FooNode(private val interactionSource: InteractionSource) : Modifier.Node(), DrawModifierNode {
        override fun onAttach() {
            coroutineScope.launch {
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> {
                            println("Press")
                        }
                        is PressInteraction.Release -> {
                            println("Release")
                        }
                        is PressInteraction.Cancel -> {
                            println("Cancel")
                        }
                        else -> {
                            println("Unknown interaction: ${interaction::class.java.name}")
                        }
                    }
                }
            }
        }

        override fun ContentDrawScope.draw() {
            drawContent()
        }
    }

    private data object FooIndicationNodeFactory : IndicationNodeFactory {
        override fun create(interactionSource: InteractionSource): DelegatableNode {
            return FooNode(interactionSource = interactionSource)
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().align(Alignment.Center)) {
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                showToast("foo")
                            }
                            .wrapContentSize(),
                        text = "foo",
                    )
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .onClick {
                                showToast("bar")
                            }
                            .wrapContentSize(),
                        text = "bar",
                    )
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .onClick(
                                enabled = true,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = FooIndicationNodeFactory,
                                block = {
                                    showToast("baz")
                                },
                            )
                            .wrapContentSize(),
                        text = "baz",
                    )
                }
            }
        }
    }
}
