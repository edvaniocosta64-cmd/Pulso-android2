package com.example.game

import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

object BoardEngine {

    private val nextId = AtomicLong(1L)

    fun newTile(kind: TileKind): GameTile = GameTile(
        id = nextId.getAndIncrement(),
        kind = kind
    )

    fun randomTile(random: Random = Random.Default): GameTile =
        newTile(TileKind.entries[random.nextInt(TileKind.entries.size)])

    fun createPlayableBoard(
        rows: Int = 4,
        cols: Int = 4,
        random: Random = Random.Default
    ): List<List<GameTile>> {
        var grid = List(rows) { List(cols) { randomTile(random) } }
        if (findAllValidClusters(grid).isEmpty()) {
            grid = forceValidMove(grid, random)
        }
        return grid
    }

    fun createStep1TutorialBoard(): Pair<List<List<GameTile>>, Set<BoardPos>> {
        val layout = listOf(
            listOf(TileKind.ONDA, TileKind.CHAMA, TileKind.FOLHA),
            listOf(TileKind.SOLAR, TileKind.SOLAR, TileKind.ONDA),
            listOf(TileKind.FOLHA, TileKind.CHAMA, TileKind.FOLHA)
        )
        val grid = layout.map { row -> row.map { kind -> newTile(kind) } }
        val spotlight = setOf(BoardPos(1, 0), BoardPos(1, 1))
        return grid to spotlight
    }

    fun createStep2TutorialBoard(): Pair<List<List<GameTile>>, Set<BoardPos>> {
        val layout = listOf(
            listOf(TileKind.SOLAR, TileKind.CHAMA, TileKind.FOLHA),
            listOf(TileKind.ONDA, TileKind.ONDA, TileKind.ONDA),
            listOf(TileKind.CHAMA, TileKind.ONDA, TileKind.SOLAR)
        )
        val grid = layout.map { row -> row.map { kind -> newTile(kind) } }
        val spotlight = setOf(
            BoardPos(1, 0),
            BoardPos(1, 1),
            BoardPos(1, 2),
            BoardPos(2, 1)
        )
        return grid to spotlight
    }

    fun createStep3TutorialBoard(): Pair<List<List<GameTile>>, Set<BoardPos>> {
        val layout = listOf(
            listOf(TileKind.CHAMA, TileKind.ONDA, TileKind.CHAMA),
            listOf(TileKind.FOLHA, TileKind.CHAMA, TileKind.SOLAR),
            listOf(TileKind.ONDA, TileKind.FOLHA, TileKind.CHAMA)
        )
        val grid = layout.map { row -> row.map { kind -> newTile(kind) } }
        val spotlight = setOf(
            BoardPos(0, 0),
            BoardPos(0, 2),
            BoardPos(1, 1),
            BoardPos(2, 2)
        )
        return grid to spotlight
    }

    fun findConnectedCluster(grid: List<List<GameTile>>, start: BoardPos): Set<BoardPos> {
        if (grid.isEmpty() || start.row !in grid.indices || start.col !in grid[start.row].indices) {
            return emptySet()
        }
        val targetKind = grid[start.row][start.col].kind
        val visited = mutableSetOf<BoardPos>()
        val queue = ArrayDeque<BoardPos>()
        queue.add(start)
        visited.add(start)

        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for ((dr, dc) in directions) {
                val nr = current.row + dr
                val nc = current.col + dc
                val next = BoardPos(nr, nc)
                if (nr in grid.indices &&
                    nc in grid[nr].indices &&
                    next !in visited &&
                    grid[nr][nc].kind == targetKind
                ) {
                    visited.add(next)
                    queue.add(next)
                }
            }
        }
        return visited
    }

    fun findAllValidClusters(grid: List<List<GameTile>>): List<Set<BoardPos>> {
        val seen = mutableSetOf<BoardPos>()
        val clusters = mutableListOf<Set<BoardPos>>()
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                val pos = BoardPos(r, c)
                if (pos !in seen) {
                    val cluster = findConnectedCluster(grid, pos)
                    seen.addAll(cluster)
                    if (cluster.size >= 2) {
                        clusters.add(cluster)
                    }
                }
            }
        }
        return clusters
    }

    fun findBestHintCluster(grid: List<List<GameTile>>): Set<BoardPos> {
        return findAllValidClusters(grid).maxByOrNull { it.size } ?: emptySet()
    }

    fun calculateClusterPoints(clusterSize: Int): Int {
        if (clusterSize < 2) return 0
        val base = clusterSize * 10
        val bonus = if (clusterSize >= 3) (clusterSize - 2) * 10 else 0
        return base + bonus
    }

    fun removePositionsAndRefill(
        grid: List<List<GameTile>>,
        toRemove: Set<BoardPos>,
        ensurePlayableMove: Boolean = true,
        random: Random = Random.Default
    ): Pair<List<List<GameTile>>, Boolean> {
        if (grid.isEmpty() || toRemove.isEmpty()) return grid to false
        val rows = grid.size
        val cols = grid[0].size

        val mutableCols = Array(cols) { c ->
            val surviving = mutableListOf<GameTile>()
            for (r in 0 until rows) {
                if (BoardPos(r, c) !in toRemove) {
                    surviving.add(grid[r][c])
                }
            }
            val missing = rows - surviving.size
            val fresh = List(missing) { randomTile(random) }
            (fresh + surviving)
        }

        var nextGrid = List(rows) { r ->
            List(cols) { c -> mutableCols[c][r] }
        }

        var wasReshuffled = false
        if (ensurePlayableMove && findAllValidClusters(nextGrid).isEmpty()) {
            nextGrid = reshuffleUntilPlayable(nextGrid, random)
            wasReshuffled = true
        }
        return nextGrid to wasReshuffled
    }

    fun findAllOfKind(grid: List<List<GameTile>>, kind: TileKind): Set<BoardPos> {
        val result = mutableSetOf<BoardPos>()
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                if (grid[r][c].kind == kind) {
                    result.add(BoardPos(r, c))
                }
            }
        }
        return result
    }

    private fun reshuffleUntilPlayable(
        grid: List<List<GameTile>>,
        random: Random
    ): List<List<GameTile>> {
        val rows = grid.size
        val cols = grid[0].size
        val flat = grid.flatten().toMutableList()
        repeat(6) {
            flat.shuffle(random)
            val candidate = List(rows) { r -> List(cols) { c -> flat[r * cols + c] } }
            if (findAllValidClusters(candidate).isNotEmpty()) {
                return candidate
            }
        }
        return forceValidMove(grid, random)
    }

    private fun forceValidMove(
        grid: List<List<GameTile>>,
        random: Random
    ): List<List<GameTile>> {
        val mutable = grid.map { it.toMutableList() }.toMutableList()
        val r = random.nextInt(mutable.size)
        val c = random.nextInt((mutable[0].size - 1).coerceAtLeast(1))
        val targetKind = mutable[r][c].kind
        if (c + 1 < mutable[r].size) {
            mutable[r][c + 1] = newTile(targetKind)
        }
        return mutable.map { it.toList() }
    }
}
