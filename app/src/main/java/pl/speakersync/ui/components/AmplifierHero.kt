package pl.speakersync.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.speakersync.ui.theme.rememberMusicBetterFontFamily
import pl.speakersync.ui.theme.rememberMusicBetterSubtitleFontFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val PanelBlack = Color(0xFF050505)
private val GlassBlack = Color(0xFF101010)
private val KnobSilverLight = Color(0xFFFAFAFE)
private val KnobSilverMid = Color(0xFFE8E8F2)
private val KnobSilverDark = Color(0xFFB8B8C8)
private val IndicatorGreen = Color(0xFF39FF14)
private val MeterBlue = Color(0xFF1248D8)
private val MeterBlueBright = Color(0xFF2A7BFF)
private val MeterBlueDark = Color(0xFF082878)

@Composable
fun AmplifierHero(
    isMusicActive: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "mc_panel")

    val leftNeedle by transition.animateFloat(
        initialValue = -38f,
        targetValue = if (isMusicActive) 32f else -12f,
        animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "needle_l"
    )
    val rightNeedle by transition.animateFloat(
        initialValue = 34f,
        targetValue = if (isMusicActive) -28f else 10f,
        animationSpec = infiniteRepeatable(tween(610, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "needle_r"
    )
    val leftPot by transition.animateFloat(
        initialValue = -50f,
        targetValue = if (isMusicActive) 35f else -5f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pot_l"
    )
    val rightPot by transition.animateFloat(
        initialValue = 48f,
        targetValue = if (isMusicActive) -30f else 8f,
        animationSpec = infiniteRepeatable(tween(780, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pot_r"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(listOf(GlassBlack, PanelBlack, Color(0xFF000000)))
                )
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    VuMeter(
                        needleAngle = leftNeedle,
                        isActive = isMusicActive,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    VuMeter(
                        needleAngle = rightNeedle,
                        isActive = isMusicActive,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                MusicBetterLogo(isActive = isMusicActive)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PowerGuardIndicator(label = "L", isActive = isMusicActive)
                    PowerGuardIndicator(label = "R", isActive = isMusicActive)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    McIntoshPotentiometer(
                        angleDegrees = leftPot,
                        label = "METER",
                        options = "LIGHTS OFF · WATTS · HOLD",
                        isActive = isMusicActive
                    )
                    McIntoshPotentiometer(
                        angleDegrees = rightPot,
                        label = "POWER",
                        options = "OFF · REMOTE · ON",
                        isActive = isMusicActive
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicBetterLogo(isActive: Boolean) {
    val logoFont = rememberMusicBetterFontFamily()
    val subtitleFont = rememberMusicBetterSubtitleFontFamily()
    val glow = if (isActive) IndicatorGreen else IndicatorGreen.copy(alpha = 0.75f)
    val textShadow = Shadow(color = glow.copy(alpha = 0.85f), blurRadius = 16f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Music is Better",
            fontFamily = logoFont,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = glow,
            style = TextStyle(shadow = textShadow),
            textAlign = TextAlign.Center
        )
        Text(
            text = "STEREO POWER AMPLIFIER",
            fontFamily = subtitleFont,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 3.sp,
            color = glow.copy(alpha = 0.9f),
            style = TextStyle(shadow = textShadow.copy(blurRadius = 8f)),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun VuMeter(
    needleAngle: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        MeterBlueBright.copy(alpha = if (isActive) 1f else 0.65f),
                        MeterBlue,
                        MeterBlueDark
                    )
                ),
                size = size
            )

            val cx = size.width / 2f
            val cy = size.height * 0.92f
            val arcRadius = size.width * 0.38f

            for (tick in -5..0) {
                val angle = -180.0 + (tick + 5) * 18.0
                val rad = angle * PI / 180.0
                val inner = arcRadius * 0.72f
                val outer = arcRadius * 0.92f
                drawLine(
                    color = Color.White.copy(alpha = 0.55f),
                    start = Offset(
                        cx + inner * cos(rad).toFloat(),
                        cy + inner * sin(rad).toFloat()
                    ),
                    end = Offset(
                        cx + outer * cos(rad).toFloat(),
                        cy + outer * sin(rad).toFloat()
                    ),
                    strokeWidth = 1.5f
                )
            }

            rotate(needleAngle + 90f, Offset(cx, cy)) {
                drawLine(
                    color = Color.Black,
                    start = Offset(cx, cy),
                    end = Offset(cx, cy - arcRadius * 0.88f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        Text(
            text = "POWER OUTPUT",
            color = MeterBlueBright.copy(alpha = 0.9f),
            fontSize = 7.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("WATTS", color = Color.White.copy(0.5f), fontSize = 6.sp)
            Text("dB", color = Color.White.copy(0.5f), fontSize = 6.sp)
        }
    }
}

@Composable
private fun PowerGuardIndicator(label: String, isActive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isActive) IndicatorGreen else Color(0xFF1A3A12))
        )
        Text(
            text = "POWER GUARD $label",
            color = if (isActive) IndicatorGreen.copy(0.85f) else Color(0xFF3A5038),
            fontSize = 7.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun McIntoshPotentiometer(
    angleDegrees: Float,
    label: String,
    options: String,
    isActive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(130.dp)) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(KnobSilverLight, KnobSilverMid, KnobSilverDark),
                    center = center,
                    radius = radius
                ),
                radius = radius * 0.94f,
                center = center
            )

            drawCircle(
                color = Color(0xFF9090A0),
                radius = radius * 0.94f,
                center = center,
                style = Stroke(width = radius * 0.05f)
            )

            for (i in 0 until 40) {
                val rad = i * 360.0 / 40 * PI / 180.0
                drawLine(
                    color = KnobSilverDark.copy(alpha = 0.45f),
                    start = Offset(
                        center.x + radius * 0.76f * cos(rad).toFloat(),
                        center.y + radius * 0.76f * sin(rad).toFloat()
                    ),
                    end = Offset(
                        center.x + radius * 0.90f * cos(rad).toFloat(),
                        center.y + radius * 0.90f * sin(rad).toFloat()
                    ),
                    strokeWidth = 1f
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF454550), Color(0xFF18181E)),
                    center = center,
                    radius = radius * 0.52f
                ),
                radius = radius * 0.52f,
                center = center
            )

            rotate(angleDegrees, center) {
                drawLine(
                    color = if (isActive) IndicatorGreen else Color(0xFFD8D8E4),
                    start = center,
                    end = Offset(center.x, center.y - radius * 0.58f),
                    strokeWidth = radius * 0.065f,
                    cap = StrokeCap.Round
                )
            }

            if (isActive) {
                drawArc(
                    color = IndicatorGreen.copy(alpha = 0.22f),
                    startAngle = angleDegrees - 20f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = radius * 0.07f)
                )
            }
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = if (isActive) IndicatorGreen else Color(0xFF777788),
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = options,
            fontSize = 6.sp,
            letterSpacing = 0.3.sp,
            color = Color(0xFF555566),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
