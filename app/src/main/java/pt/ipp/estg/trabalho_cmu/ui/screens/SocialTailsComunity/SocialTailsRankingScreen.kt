package pt.ipp.estg.trabalho_cmu.ui.screens.SocialTailsComunity

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import pt.ipp.estg.trabalho_cmu.ui.components.RankingCard
import pt.ipp.estg.trabalho_cmu.ui.theme.BronzeTrophy
import pt.ipp.estg.trabalho_cmu.ui.theme.GrayTrophy
import pt.ipp.estg.trabalho_cmu.ui.theme.YellowTrophy

@Composable
fun SocialTailsRankingScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Dados de exemplo do ranking
    val overallRanking = listOf(
        RankingData(
            position = 1,
            name = stringResource(R.string.ranking_overall_1_name),
            distance = stringResource(R.string.ranking_overall_1_distance),
            trophyColor = YellowTrophy
        ),
        RankingData(
            position = 2,
            name = stringResource(R.string.ranking_overall_2_name),
            distance = stringResource(R.string.ranking_overall_2_distance),
            trophyColor = GrayTrophy
        ),
        RankingData(
            position = 3,
            name = stringResource(R.string.ranking_overall_3_name),
            distance = stringResource(R.string.ranking_overall_3_distance),
            trophyColor = BronzeTrophy
        )
    )

    val monthlyRanking = listOf(
        RankingData(
            position = 1,
            name = stringResource(R.string.ranking_overall_1_name),
            distance = stringResource(R.string.ranking_overall_1_distance),
            trophyColor = YellowTrophy
        ),
        RankingData(
            position = 2,
            name = stringResource(R.string.ranking_overall_2_name),
            distance = stringResource(R.string.ranking_overall_2_distance),
            trophyColor = GrayTrophy
        ),
        RankingData(
            position = 3,
            name = stringResource(R.string.ranking_overall_3_name),
            distance = stringResource(R.string.ranking_overall_3_distance),
            trophyColor = BronzeTrophy
        )
    )

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
            horizontalAlignment = Alignment.Start
        ) {
            // Título
            Text(
                text = stringResource(id = R.string.socialtails_ranking),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Secção "De Sempre"
            Text(
                text = stringResource(id = R.string.forever),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            overallRanking.forEach { rank ->
                RankingCard(rank = rank)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seção "Neste Mês"
            Text(
                text = stringResource(id = R.string.this_month),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            monthlyRanking.forEach { rank ->
                RankingCard(rank = rank)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}


data class RankingData(
    val position: Int,
    val name: String,
    val distance: String,
    val trophyColor: Color
)

@Preview(showBackground = true)
@Composable
fun SocialTailsRankingScreenPreview() {
    SocialTailsRankingScreen()
}