package pt.ipp.estg.trabalho_cmu.ui.screens.SocialTailsComunity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.ui.components.CommunityCard
import pt.ipp.estg.trabalho_cmu.ui.theme.YellowTrophy

@Composable
fun SocialTailsCommunityScreen(
    onViewRanking: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = stringResource(id = R.string.socialtails_comunity),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CommunityCard(
                activityText = stringResource(id = R.string.activity_1),
                trophyColor = YellowTrophy,
                mapImage = R.drawable.map_image1
            )

            Spacer(modifier = Modifier.height(16.dp))

            CommunityCard(
                activityText = stringResource(id = R.string.activity_2),
                trophyColor = YellowTrophy,
                mapImage = R.drawable.map_image2
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onViewRanking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C8B7E),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.view_ranking),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SocialTailsCommunityScreenPreview() {
    SocialTailsCommunityScreen()
}