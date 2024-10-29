import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gammaplay.findmyphone.R

@Composable
fun CustomRateUsDialog(
    onDismiss: () -> Unit, onSubmit: (Int) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 5.dp, vertical = 20.dp)
                    .fillMaxWidth()
            ) {
                // Icon
                Icon(
                    imageVector = Icons.Filled.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(35.dp)
                        .align(Alignment.CenterHorizontally),
                    tint = Color.Black
                )

                // Title
                Text(
                    text = stringResource(id = R.string.rate_us_title), // "How do you like this app?"
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Star Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { rating ->
                        Icon(
                            imageVector = if (rating <= selectedRating) Icons.Outlined.StarBorder else Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier
                                .size(45.dp)
                                .padding(8.dp)
                                .clickable {
                                    selectedRating = rating
                                },
                            tint = if (rating <= selectedRating) colorResource(id = R.color.title_text)
                            else colorResource(id = R.color.accent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()) {
                    // Submit Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(2.dp)
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text(text = stringResource(id = R.string.not_now_button)) // "Submit"
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onSubmit(selectedRating) },
                        modifier = Modifier
                            .padding(2.dp)
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.accent))
                    ) {
                        Text(text = stringResource(id = R.string.submit)) // "Submit"
                    }

                }


            }
        }
    }
}
