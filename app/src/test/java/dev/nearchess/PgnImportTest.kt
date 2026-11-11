package dev.nearchess
import org.junit.Test
import org.junit.Assert.*
class PgnImportTest {
 @Test fun commentsAndVariations(){val g=PgnImport.parse("1.e4 {hello} e5 (1... c5 (2. Nf3)) 2.Nf3 $1 Nc6 *");assertEquals(4,g.notation.size);assertEquals(5,g.positions.size)}
 @Test fun fenMate(){val g=PgnImport.parse("[SetUp \"1\"]\n[FEN \"7k/5Q2/6K1/8/8/8/8/8 w - - 0 1\"]\n[Result \"1-0\"]\n1. Qg7# 1-0");assertTrue(g.positions.last().legal().isEmpty())}
 @Test(expected=IllegalArgumentException::class) fun illegalMove(){PgnImport.parse("1. e5 *")}
 @Test(expected=IllegalArgumentException::class) fun brokenVariation(){PgnImport.parse("1. e4 (1. d4 *")}
 @Test(expected=IllegalArgumentException::class) fun resultMismatch(){PgnImport.parse("[Result \"1-0\"]\n1. e4 0-1")}
}
