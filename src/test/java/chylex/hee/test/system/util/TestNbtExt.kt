package chylex.hee.test.system.util
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.NBTPrimitiveList
import chylex.hee.system.util.getListOfByteArrays
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getListOfIntArrays
import chylex.hee.system.util.getListOfPrimitives
import chylex.hee.system.util.getListOfStrings
import chylex.hee.system.util.nbt
import chylex.hee.system.util.nbtOrNull
import net.minecraft.init.Bootstrap
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagByte
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagDouble
import net.minecraft.nbt.NBTTagFloat
import net.minecraft.nbt.NBTTagInt
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagLong
import net.minecraft.nbt.NBTTagShort
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestNbtExt{
	init{
		Bootstrap.register()
	}
	
	@Nested inner class ItemStacks{
		@Nested inner class Nbt{
			@Test fun `'nbt' returns an existing tag`(){
				val stack = ItemStack(Items.BOW).apply { setStackDisplayName("Hello") }
				assertEquals("Hello", stack.nbt.getCompoundTag("display").getString("Name"))
			}
			
			@Test fun `'nbt' assigns a new tag if missing`(){
				val stack = ItemStack(Items.BOW)
				assertNull(stack.tagCompound)
				
				assertEquals(0, stack.nbt.size)
				assertNotNull(stack.tagCompound)
				
				stack.nbt.setString("key", "Hello")
				assertEquals("Hello", stack.tagCompound?.getString("key"))
			}
		}
		
		@Nested inner class NbtOrNull{
			@Test fun `'nbtOrNull' returns an existing tag`(){
				val stack = ItemStack(Items.BOW).apply { setStackDisplayName("Hello") }
				assertEquals("Hello", stack.nbtOrNull?.getCompoundTag("display")?.getString("Name"))
			}
			
			@Test fun `'nbtOrNull' returns null if tag is missing`(){
				val stack = ItemStack(Items.BOW)
				assertNull(stack.nbtOrNull)
				assertNull(stack.tagCompound)
			}
		}
	}
	
	@Nested inner class NBTPrimitiveLists{
		@Nested inner class Properties{
			@Test fun `'isEmpty" returns true for empty tags`(){
				val list = NBTTagCompound().getListOfPrimitives("key")
				assertTrue(list.isEmpty)
			}
			
			@Test fun `'isEmpty' returns false for non-empty tags`(){
				val list = NBTTagCompound().getListOfPrimitives("key")
				
				list.append(1)
				list.append(2)
				Assertions.assertFalse(list.isEmpty)
			}
			
			@Test fun `'size' returns correct amount of tags`(){
				val list = NBTTagCompound().getListOfPrimitives("key")
				
				assertEquals(0, list.size)
				list.append(5)
				assertEquals(1, list.size)
				list.append(5)
				assertEquals(2, list.size)
			}
		}
		
		@Nested inner class AppendGet{
			private inline fun <reified T : Any> testAppendGet(callAppend: (NBTPrimitiveList) -> Unit, callGetCheck: (T) -> Boolean){
				with(NBTTagCompound()){
					val list = getListOfPrimitives("key")
					
					callAppend(list)
					setList("key", list)
					
					val primitive1 = (getTag("key") as NBTTagList).get(0)
					assertTrue(primitive1 is T)
					assertTrue(callGetCheck(primitive1 as T))
					
					val primitive2 = list.get(0)
					assertTrue(primitive2 is T)
					assertTrue(callGetCheck(primitive2 as T))
				}
			}
			
			@Test fun `'append' using byte updates the tag and 'get' returns the same value`(){
				testAppendGet<NBTTagByte>(
					{ list -> list.append(Byte.MAX_VALUE) },
					{ primitive -> primitive.byte == Byte.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using short updates the tag and 'get' returns the same value`(){
				testAppendGet<NBTTagShort>(
					{ list -> list.append(Short.MAX_VALUE) },
					{ primitive -> primitive.short == Short.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using int updates the tag and 'get' returns the same value`(){
				testAppendGet<NBTTagInt>(
					{ list -> list.append(Int.MAX_VALUE) },
					{ primitive -> primitive.int == Int.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using long updates the tag and 'get' returns the same value`(){
				testAppendGet<NBTTagLong>({
					list -> list.append(Long.MAX_VALUE) },
					{ primitive -> primitive.long == Long.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using float updates the tag and 'get' returns the same value`(){
				testAppendGet<NBTTagFloat>(
					{ list -> list.append(Float.MAX_VALUE) },
					{ primitive -> primitive.float == Float.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using double updates the tag and 'get' returns the same value`(){
				testAppendGet<NBTTagDouble>(
					{ list -> list.append(Double.MAX_VALUE) },
					{ primitive -> primitive.double == Double.MAX_VALUE }
				)
			}
		}
		
		@Nested inner class Get{
			private fun makeFilledTestList() = NBTTagCompound().getListOfPrimitives("key").apply {
				append(1)
				append(2)
				append(3)
			}
			
			@Test fun `'get' returns correct value`(){
				val list = makeFilledTestList()
				assertEquals(NBTTagInt(1), list.get(0))
				assertEquals(NBTTagInt(2), list.get(1))
				assertEquals(NBTTagInt(3), list.get(2))
			}
			
			@Test fun `'get' throws when out of bounds`(){
				val list = makeFilledTestList()
				assertThrows<IndexOutOfBoundsException> { list.get(-1) }
				assertThrows<IndexOutOfBoundsException> { list.get(3) }
			}
		}
		
		@Nested inner class Iterators{
			private fun makeFilledTestList() = NBTTagCompound().getListOfPrimitives("key").apply {
				append(1)
				append(2)
				append(3)
				append(Int.MIN_VALUE)
				append(Int.MAX_VALUE)
			}
			
			@Test fun `iterating goes through all items`(){
				val list = makeFilledTestList()
				assertIterableEquals(intArrayOf(1, 2, 3, Int.MIN_VALUE, Int.MAX_VALUE).map(::NBTTagInt).asIterable(), list)
			}
			
			@Test fun `'hasNext' returns false and 'next' throws if empty`(){
				val list = NBTTagCompound().getListOfPrimitives("key")
				val iterator = list.iterator()
				
				assertFalse(iterator.hasNext())
				assertThrows<NoSuchElementException> { iterator.next() }
			}
			
			@Test fun `'hasNext' returns true and 'next' returns value if non-empty`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertTrue(iterator.hasNext())
				assertEquals(NBTTagInt(1), iterator.next())
			}
			
			@Test fun `'remove' throws before calling 'next'`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertThrows<IllegalStateException> { iterator.remove() }
			}
			
			@Test fun `'remove' removes first element correctly`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertEquals(NBTTagInt(1), iterator.next())
				iterator.remove()
				assertEquals(NBTTagInt(2), iterator.next())
				
				assertIterableEquals(intArrayOf(2, 3, Int.MIN_VALUE, Int.MAX_VALUE).map(::NBTTagInt).asIterable(), list)
			}
			
			@Test fun `'remove' removes several elements correctly`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertEquals(NBTTagInt(1), iterator.next())
				assertEquals(NBTTagInt(2), iterator.next())
				iterator.remove()
				assertEquals(NBTTagInt(3), iterator.next())
				assertEquals(NBTTagInt(Int.MIN_VALUE), iterator.next())
				iterator.remove()
				assertEquals(NBTTagInt(Int.MAX_VALUE), iterator.next())
				
				assertIterableEquals(intArrayOf(1, 3, Int.MAX_VALUE).map(::NBTTagInt).asIterable(), list)
			}
		}
		
		@Nested inner class SequenceGetters{
			private inline fun <T : Any> testSequenceGetter(callAppend: (NBTPrimitiveList, T) -> Unit, callGetSequence: (NBTPrimitiveList) -> Sequence<T>, testValues: Array<T>){
				with(NBTTagCompound()){
					val list = getListOfPrimitives("key")
					
					for(element in testValues){
						callAppend(list, element)
					}
					
					assertIterableEquals(testValues.asIterable(), callGetSequence(list).asIterable())
				}
			}
			
			@Test fun `'allBytes' retrieves all values`(){
				testSequenceGetter(
					NBTPrimitiveList::append,
					NBTPrimitiveList::allBytes,
					arrayOf(1.toByte(), 2.toByte(), 3.toByte(), Byte.MIN_VALUE, Byte.MAX_VALUE)
				)
			}
			
			@Test fun `'allShorts' retrieves all values`(){
				testSequenceGetter(
					NBTPrimitiveList::append,
					NBTPrimitiveList::allShorts,
					arrayOf(1.toShort(), 2.toShort(), 3.toShort(), Short.MIN_VALUE, Short.MAX_VALUE)
				)
			}
			
			@Test fun `'allInts' retrieves all values`(){
				testSequenceGetter(
					NBTPrimitiveList::append,
					NBTPrimitiveList::allInts,
					arrayOf(1, 2, 3, Int.MIN_VALUE, Int.MAX_VALUE)
				)
			}
			
			@Test fun `'allLongs' retrieves all values`(){
				testSequenceGetter(
					NBTPrimitiveList::append,
					NBTPrimitiveList::allLongs,
					arrayOf(1L, 2L, 3L, Long.MIN_VALUE, Long.MAX_VALUE)
				)
			}
			
			@Test fun `'allFloats' retrieves all values`(){
				testSequenceGetter(
					NBTPrimitiveList::append,
					NBTPrimitiveList::allFloats,
					arrayOf(1F, 2F, 3F, Float.MIN_VALUE, Float.MAX_VALUE)
				)
			}
			
			@Test fun `'allDoubles' retrieves all values`(){
				testSequenceGetter(
					NBTPrimitiveList::append,
					NBTPrimitiveList::allDoubles,
					arrayOf(1.0, 2.0, 3.0, Double.MIN_VALUE, Double.MAX_VALUE)
				)
			}
		}
	}
	
	@Nested inner class NBTObjectLists{
		@Nested inner class Properties{
			@Test fun `'isEmpty' returns true for empty tags`(){
				val list2 = NBTTagCompound().getListOfStrings("key")
				assertTrue(list2.isEmpty)
			}
			
			@Test fun `'isEmpty' returns false for non-empty tags`(){
				val list2 = NBTTagCompound().getListOfStrings("key")
				
				list2.append("test")
				Assertions.assertFalse(list2.isEmpty)
			}
			
			@Test fun `'size' returns correct amount of tags`(){
				val list2 = NBTTagCompound().getListOfStrings("key")
				
				assertEquals(0, list2.size)
				list2.append("first")
				assertEquals(1, list2.size)
				list2.append("second")
				assertEquals(2, list2.size)
			}
		}
		
		@Nested inner class AppendGet{
			private inline fun <T : Any> testAppendGet(listGetter: NBTTagCompound.(String) -> NBTObjectList<T>, callAppend: (NBTObjectList<T>) -> Unit, callGetCheck: (T) -> Boolean){
				with(NBTTagCompound()){
					val list = listGetter("key")
					
					callAppend(list)
					setList("key", list)
					
					assertTrue((getTag("key") as NBTTagList).get(0) is NBTBase)
					assertTrue(callGetCheck(list.get(0)))
				}
			}
			
			@Test fun `'append' using NBTTagCompound updates the tag and 'get' returns the same value`(){
				testAppendGet(
					NBTTagCompound::getListOfCompounds,
					{ list -> list.append(NBTTagCompound().apply { setInteger("test", 123) }) },
					{ value -> value.getInteger("test") == 123 }
				)
			}
			
			@Test fun `'append' using String updates the tag and 'get' returns the same value`(){
				testAppendGet(
					NBTTagCompound::getListOfStrings,
					{ list -> list.append("hello") },
					{ value -> value == "hello" }
				)
			}
			
			@Test fun `'append' using ByteArray updates the tag and 'get' returns the same value`(){
				testAppendGet(
					NBTTagCompound::getListOfByteArrays,
					{ list -> list.append(byteArrayOf(1, 2, 3)) },
					{ value -> value.contentEquals(byteArrayOf(1, 2, 3)) }
				)
			}
			
			@Test fun `'append' using IntArray updates the tag and 'get' returns the same value`(){
				testAppendGet(
					NBTTagCompound::getListOfIntArrays,
					{ list -> list.append(intArrayOf(1, 2, 3)) },
					{ value -> value.contentEquals(intArrayOf(1, 2, 3)) }
				)
			}
		}
		
		@Nested inner class Get{
			private fun makeFilledTestList() = NBTTagCompound().getListOfStrings("key").apply {
				append("a")
				append("b")
				append("c")
			}
			
			@Test fun `'get' returns correct value`(){
				val list = makeFilledTestList()
				assertEquals("a", list.get(0))
				assertEquals("b", list.get(1))
				assertEquals("c", list.get(2))
			}
			
			@Test fun `'get' throws when out of bounds`(){
				val list = makeFilledTestList()
				assertThrows<IndexOutOfBoundsException> { list.get(-1) }
				assertThrows<IndexOutOfBoundsException> { list.get(3) }
			}
		}
		
		@Nested inner class Iterators{
			private fun makeFilledTestList() = NBTTagCompound().getListOfStrings("key").apply {
				append("a")
				append("b")
				append("c")
				append("")
				append("123")
			}
			
			@Test fun `iterating goes through all items`(){
				val list = makeFilledTestList()
				assertIterableEquals(arrayOf("a", "b", "c", "", "123").asIterable(), list)
			}
			
			@Test fun `'hasNext' returns false and 'next' throws if empty`(){
				val list = NBTTagCompound().getListOfPrimitives("key")
				val iterator = list.iterator()
				
				assertFalse(iterator.hasNext())
				assertThrows<NoSuchElementException> { iterator.next() }
			}
			
			@Test fun `'hasNext' returns true and 'next' returns value if non-empty`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertTrue(iterator.hasNext())
				assertEquals("a", iterator.next())
			}
			
			@Test fun `'remove' throws before calling 'next'`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertThrows<IllegalStateException> { iterator.remove() }
			}
			
			@Test fun `'remove' removes first element correctly`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertEquals("a", iterator.next())
				iterator.remove()
				assertEquals("b", iterator.next())
				
				assertIterableEquals(arrayOf("b", "c", "", "123").asIterable(), list)
			}
			
			@Test fun `'remove' removes several elements correctly`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertEquals("a", iterator.next())
				assertEquals("b", iterator.next())
				iterator.remove()
				assertEquals("c", iterator.next())
				assertEquals("", iterator.next())
				iterator.remove()
				assertEquals("123", iterator.next())
				
				assertIterableEquals(arrayOf("a", "c", "123").asIterable(), list)
			}
		}
	}
}
