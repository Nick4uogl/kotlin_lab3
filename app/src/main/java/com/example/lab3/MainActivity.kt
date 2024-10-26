package com.example.lab3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolarCalculator()
        }
    }
}

data class CalculationResult(
    // Початкова система
    val energyShareWithoutImbalance: Double,
    val energyWithoutImbalance: Double,
    val profit: Double,
    val energyWithImbalance: Double,
    val penalty: Double,
    // Покращена система
    val improvedEnergyShareWithoutImbalance: Double,
    val improvedEnergyWithoutImbalance: Double,
    val improvedProfit: Double,
    val improvedEnergyWithImbalance: Double,
    val improvedPenalty: Double,
    val totalProfit: Double
)

@Composable
fun SolarCalculator() {
    var pc by remember { mutableStateOf("5.0") }
    var sigma by remember { mutableStateOf("0.25") }
    var pricePerMWh by remember { mutableStateOf("7.0") }

    var result by remember { mutableStateOf<CalculationResult?>(null) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Калькулятор прибутку сонячної електростанції",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = pc,
            onValueChange = { pc = it },
            label = { Text("Середня потужність (МВт)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sigma,
            onValueChange = { sigma = it },
            label = { Text("Стандартне відхилення") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pricePerMWh,
            onValueChange = { pricePerMWh = it },
            label = { Text("Тариф (тис. грн/МВт⋅год)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                try {
                    result = calculateProfit(
                        pc = pc.toDouble(),
                        sigma = sigma.toDouble(),
                        pricePerMWh = pricePerMWh.toDouble()
                    )
                } catch (e: Exception) {
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Розрахувати")
        }

        result?.let { res ->
            ResultCard(res)
        }
    }
}

@Composable
fun ResultCard(result: CalculationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Початкова система:", fontWeight = FontWeight.Bold)
            Text("Частка енергії без небалансів: ${String.format("%.1f", result.energyShareWithoutImbalance)}%")
            Text("Енергія без небалансів: ${String.format("%.1f", result.energyWithoutImbalance)} МВт⋅год")
            Text("Прибуток: ${String.format("%.1f", result.profit)} тис. грн")
            Text("Енергія з небалансами: ${String.format("%.1f", result.energyWithImbalance)} МВт⋅год")
            Text("Штраф: ${String.format("%.1f", result.penalty)} тис. грн")

            Spacer(modifier = Modifier.height(16.dp))

            Text("Покращена система:", fontWeight = FontWeight.Bold)
            Text("Частка енергії без небалансів: ${String.format("%.1f", result.improvedEnergyShareWithoutImbalance)}%")
            Text("Енергія без небалансів: ${String.format("%.1f", result.improvedEnergyWithoutImbalance)} МВт⋅год")
            Text("Прибуток: ${String.format("%.1f", result.improvedProfit)} тис. грн")
            Text("Енергія з небалансами: ${String.format("%.1f", result.improvedEnergyWithImbalance)} МВт⋅год")
            Text("Штраф: ${String.format("%.1f", result.improvedPenalty)} тис. грн")

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Загальний прибуток: ${String.format("%.1f", result.totalProfit)} тис. грн",
                fontWeight = FontWeight.Bold)
        }
    }
}

fun calculateProfit(pc: Double, sigma: Double, pricePerMWh: Double): CalculationResult {
    // Початкова система (формули 9.1-9.6)
    val energyShareWithoutImbalance = calculateEnergyShareWithoutImbalance(pc, sigma)
    val energyWithoutImbalance = pc * 24 * energyShareWithoutImbalance / 100
    val profit = energyWithoutImbalance * pricePerMWh
    val energyWithImbalance = pc * 24 * (1 - energyShareWithoutImbalance / 100)
    val penalty = energyWithImbalance * pricePerMWh

    // Покращена система (формули 9.7-9.11)
    val improvedSigma = sigma * 0.5 // Зменшуємо відхилення вдвічі для покращеної системи
    val improvedEnergyShareWithoutImbalance = calculateImprovedEnergyShareWithoutImbalance(pc, improvedSigma)
    val improvedEnergyWithoutImbalance = pc * 24 * improvedEnergyShareWithoutImbalance / 100 // W3 (9.8)
    val improvedProfit = improvedEnergyWithoutImbalance * pricePerMWh
    val improvedEnergyWithImbalance = pc * 24 * (1 - improvedEnergyShareWithoutImbalance / 100) // W4 (9.10)
    val improvedPenalty = improvedEnergyWithImbalance * pricePerMWh

    val totalProfit = improvedProfit - improvedPenalty

    return CalculationResult(
        energyShareWithoutImbalance = energyShareWithoutImbalance,
        energyWithoutImbalance = energyWithoutImbalance,
        profit = profit,
        energyWithImbalance = energyWithImbalance,
        penalty = penalty,
        improvedEnergyShareWithoutImbalance = improvedEnergyShareWithoutImbalance,
        improvedEnergyWithoutImbalance = improvedEnergyWithoutImbalance,
        improvedProfit = improvedProfit,
        improvedEnergyWithImbalance = improvedEnergyWithImbalance,
        improvedPenalty = improvedPenalty,
        totalProfit = totalProfit
    )
}

fun calculateEnergyShareWithoutImbalance(pc: Double, sigma: Double): Double {
    return numericalIntegration(4.75, 5.25, 1000, pc, sigma)
}

// Функція для розрахунку частки енергії без небалансів для покращеної системи (9.7)
fun calculateImprovedEnergyShareWithoutImbalance(pc: Double, sigma: Double): Double {
    // Використовуємо ті ж межі інтегрування, але з меншим sigma
    return numericalIntegration(4.75, 5.25, 1000, pc, sigma)
}


// Функція для розрахунку нормального розподілу (9.1)
fun normalDistribution(p: Double, pc: Double, sigma: Double): Double {
    return (1 / (sigma * sqrt(2 * PI))) * exp(-(p - pc).pow(2) / (2 * sigma.pow(2)))
}

// Функція для чисельного інтегрування методом трапецій
fun numericalIntegration(
    a: Double, // нижня межа
    b: Double, // верхня межа
    n: Int,    // кількість відрізків
    pc: Double,
    sigma: Double
): Double {
    val h = (b - a) / n
    var sum = (normalDistribution(a, pc, sigma) + normalDistribution(b, pc, sigma)) / 2.0

    for (i in 1 until n) {
        val x = a + i * h
        sum += normalDistribution(x, pc, sigma)
    }

    return h * sum * 100 // множимо на 100 для отримання відсотків
}