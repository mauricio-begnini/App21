package com.example.app21

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.app21.ui.theme.App21Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App21Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    App21Game()
                }
            }
        }
    }
}

@Composable
fun App21Game() {

    val scope = rememberCoroutineScope()

    var showAllCards by remember {
        mutableStateOf(false)
    }

    var status by remember {
        mutableStateOf("")
    }

    var dealerHand by remember {
        mutableStateOf(
            listOf(
                (1..13).random(),
                (1..13).random()
            )
        )
    }

    var playerHand by remember {
        mutableStateOf(
            listOf(
                (1..13).random(),
                (1..13).random(),
            )
        )
    }

    var playerPoints by remember {
        mutableStateOf(0)
    }

    var dealerPoints by remember {
        mutableStateOf(0)
    }

    var wins by remember {
        mutableStateOf(0)
    }

    var draws by remember {
        mutableStateOf(0)
    }

    var loss by remember {
        mutableStateOf(0)
    }

    val sumPoints: (List<Int>) -> Int = { cards ->
        var points = 0
        var aces = 0
        cards.forEach { card ->
            if (card == 1) {
                aces++
                points += 11
            } else if (card >= 10)
                points += 10
            else
                points += card
            if (points > 21 && aces >= 1) {
                points -= 10
                aces--
            }
        }
        points
    }

    val playerHold: () -> Unit = {
        showAllCards = true
        dealerPoints = sumPoints(dealerHand)
        playerPoints = sumPoints(playerHand)

        scope.launch {
            if (playerPoints > 21) {
                status = "You Lost by exceeding 21 points!"
                loss++
            } else {
                while (dealerPoints < 17) {
                    delay(200)
                    dealerHand = dealerHand + (1..13).random()
                    dealerPoints = sumPoints(dealerHand)
                }
                delay(100)

                if (playerPoints == 21)
                    status = "BlackJack"
                else
                    status = ""
                if (dealerPoints > 21) {
                    status += "You Won, dealer exceed 21 points!"
                    wins++
                }else if (dealerPoints == playerPoints) {
                    status += "Its a Draw, both got $playerPoints points"
                    draws++
                }else if (playerPoints > dealerPoints) {
                    status += "You Won, $playerPoints x $dealerPoints"
                    wins++
                }else {
                    status += "You Lost, $playerPoints x $dealerPoints"
                    loss++
                }
            }
        }
    }

    val playerHit: () -> Unit = {
        playerHand = playerHand + (1..13).random()
        playerPoints = sumPoints(playerHand)
        if (playerPoints >= 21)
            playerHold()
    }

    val playAgain = {
        status = ""
        showAllCards = false
        playerHand = listOf(
            (1..13).random(),
            (1..13).random(),
        )
        dealerHand = listOf(
            (1..13).random(),
            (1..13).random(),
        )
    }

    MainScreen(
        playerHand,
        dealerHand,
        showAllCards,
        status,
        wins,
        draws,
        loss,
        playerHit,
        playerHold,
        playAgain,
    )
}

@Composable
fun MainScreen(
    playerHand: List<Int>,
    houseHand: List<Int>,
    showAllCards: Boolean,
    gameStatus: String,
    wins: Int,
    draws: Int,
    loss: Int,
    playerHit: () -> Unit,
    playerHold: () -> Unit,
    playAgain: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background),
            contentDescription = "background",
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            ScoreboardScreen(wins, draws, loss)
            Cards(cards = houseHand, showAllCards = showAllCards)
            if (showAllCards)
                RestartGame(showAllCards = showAllCards, gameStatus = gameStatus, playAgain)
            Cards(cards = playerHand, showAllCards = true)
            PlayerComands(playerHit, playerHold, showAllCards)
        }
    }
}

@Composable
fun RestartGame(showAllCards: Boolean, gameStatus: String, onPlayAgainButtonClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = gameStatus)
        Button(onClick = onPlayAgainButtonClick) {
            Text(text = "Play Again")
        }
    }
}

@Composable
fun ScoreboardScreen(wins: Int, draws: Int, loss: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Text(text = "Wins: $wins")
        Text(text = "Draws: $draws")
        Text(text = "Loss: $loss")
    }
}

@Composable
fun PlayerComands(
    onHitButtonClick: () -> Unit,
    onHoldButtonClick: () -> Unit,
    showAllCards: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(onClick = onHoldButtonClick, enabled = !showAllCards) {
            Text(text = "Hold")
        }
        Button(onClick = onHitButtonClick, enabled = !showAllCards) {
            Text(text = "Hit")
        }
    }
}

@Composable
fun Cards(cards: List<Int>, showAllCards: Boolean) {
    var offset = 25
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        cards.forEachIndexed { index, card ->
            if (index == 1 && !showAllCards) {
                Card(R.drawable.back, "back", (index * offset).dp)
            } else {
                var cardDrawable = when (card) {
                    1 -> R.drawable.ace
                    2 -> R.drawable.two
                    3 -> R.drawable.three
                    4 -> R.drawable.four
                    5 -> R.drawable.five
                    6 -> R.drawable.six
                    7 -> R.drawable.seven
                    8 -> R.drawable.eight
                    9 -> R.drawable.nine
                    10 -> R.drawable.ten
                    11 -> R.drawable.jack
                    12 -> R.drawable.queen
                    else -> R.drawable.king
                }
                Card(card = cardDrawable, cardValue = "$card", (index * offset).dp)
            }
        }
    }
}

@Composable
fun Card(card: Int, cardValue: String, offset: Dp) {
    Image(
        modifier = Modifier
            .height(234.dp)
            .width(168.dp)
            .offset(offset),
        painter = painterResource(id = card),
        contentDescription = cardValue,
        contentScale = ContentScale.Fit,
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    App21Theme {
        App21Game()
    }
}