package chylex.hee.test.system.util
import chylex.hee.game.inventory.cleanupNBT
import chylex.hee.game.inventory.heeTag
import chylex.hee.game.inventory.heeTagOrNull
import chylex.hee.game.inventory.nbt
import chylex.hee.game.inventory.nbtOrNull
import chylex.hee.system.serialization.NBTBase
import chylex.hee.system.serialization.NBTList.Companion.putList
import chylex.hee.system.serialization.NBTObjectList
import chylex.hee.system.serialization.NBTPrimitiveList
import chylex.hee.system.serialization.TagByte
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.TagDouble
import chylex.hee.system.serialization.TagFloat
import chylex.hee.system.serialization.TagInt
import chylex.hee.system.serialization.TagList
import chylex.hee.system.serialization.TagLong
import chylex.hee.system.serialization.TagShort
import chylex.hee.system.serialization.getListOfByteArrays
import chylex.hee.system.serialization.getListOfCompounds
import chylex.hee.system.serialization.getListOfIntArrays
import chylex.hee.system.serialization.getListOfPrimitives
import chylex.hee.system.serialization.getListOfStrings
import chylex.hee.system.serialization.hasKey
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.heeTagOrNull
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.registry.Bootstrap
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.common.util.Constants.NBT
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
	
	@Nested inner class General{
		@Nested inner class HeeTag{
			@Test fun `'heeTag' returns an existing tag`(){
				val nbt = TagCompound().apply { put("hee", TagCompound().apply { putString("key", "Hello") }) }
				assertEquals("Hello", nbt.heeTag.getString("key"))
			}
			
			@Test fun `'heeTag' assigns a new tag if missing`(){
				val nbt = TagCompound()
				assertFalse(nbt.hasKey("hee"))
				
				nbt.heeTag.putString("key", "Hello")
				assertTrue(nbt.hasKey("hee"))
				assertEquals("Hello", nbt.heeTag.getString("key"))
			}
		}
		
		@Nested inner class HeeTagOrNull{
			@Test fun `'heeTagOrNull' returns an existing tag`(){
				val nbt = TagCompound().apply { put("hee", TagCompound().apply { putString("key", "Hello") }) }
				assertEquals("Hello", nbt.heeTagOrNull?.getString("key"))
			}
			
			@Test fun `'heeTagOrNull' returns null if tag is missing`(){
				val nbt = TagCompound()
				assertNull(nbt.heeTagOrNull)
			}
		}
	}
	
	@Nested inner class ItemStacks{
		@Nested inner class Nbt{
			@Test fun `'nbt' returns an existing ItemStack tag`(){
				val stack = ItemStack(Items.BOW).apply { displayName = StringTextComponent("Hello") }
				assertTrue(stack.nbt.hasKey("display", NBT.TAG_COMPOUND))
			}
			
			@Test fun `'nbt' assigns a new ItemStack tag if missing`(){
				val stack = ItemStack(Items.BOW)
				assertNull(stack.tag)
				
				assertEquals(0, stack.nbt.size())
				assertNotNull(stack.tag)
				
				stack.nbt.putString("key", "Hello")
				assertEquals("Hello", stack.tag?.getString("key"))
			}
		}
		
		@Nested inner class NbtOrNull{
			@Test fun `'nbtOrNull' returns an existing ItemStack tag`(){
				val stack = ItemStack(Items.BOW).apply { displayName = StringTextComponent("Hello") }
				assertTrue(stack.nbtOrNull.hasKey("display"))
			}
			
			@Test fun `'nbtOrNull' returns null if ItemStack tag is missing`(){
				val stack = ItemStack(Items.BOW)
				assertNull(stack.nbtOrNull)
				assertNull(stack.tag)
			}
		}
		
		@Nested inner class HeeTag{
			@Test fun `'heeTag' returns an existing tag`(){
				val stack = ItemStack(Items.BOW).apply { nbt.put("hee", TagCompound().apply { putString("key", "Hello") }) }
				assertEquals("Hello", stack.heeTag.getString("key"))
			}
			
			@Test fun `'heeTag' assigns a new tag if missing`(){
				val stack = ItemStack(Items.BOW).apply { heeTag.putString("key", "Hello") }
				assertEquals("Hello", stack.tag?.getCompound("hee")?.getString("key"))
			}
		}
		
		@Nested inner class HeeTagOrNull{
			@Test fun `'heeTagOrNull' returns an existing 'heeTag'`(){
				val stack = ItemStack(Items.BOW).apply { heeTag.putString("key", "Hello") }
				assertEquals("Hello", stack.heeTagOrNull?.getString("key"))
			}
			
			@Test fun `'heeTagOrNull' returns null if ItemStack tag is missing`(){
				val stack = ItemStack(Items.BOW)
				assertNull(stack.heeTagOrNull)
			}
			
			@Test fun `'heeTagOrNull' returns null if ItemStack tag is present but has no 'heeTag'`(){
				val stack = ItemStack(Items.BOW).apply { nbt.putBoolean("testing", true) }
				assertNull(stack.heeTagOrNull)
			}
		}
		
		@Nested inner class CleanupNBT{
			@Test fun `'cleanupNBT' keeps a non-empty ItemStack tag`(){
				val stack = ItemStack(Items.BOW).apply { nbt.putString("key", "Hello") }
				assertEquals("Hello", stack.nbtOrNull?.getString("key"))
				
				stack.cleanupNBT()
				assertEquals("Hello", stack.nbtOrNull?.getString("key"))
			}
			
			@Test fun `'cleanupNBT' removes an empty ItemStack tag`(){
				val stack = ItemStack(Items.BOW).apply { nbt }
				assertNotNull(stack.nbtOrNull)
				
				stack.cleanupNBT()
				assertNull(stack.nbtOrNull)
			}
			
			@Test fun `'cleanupNBT' removes an empty 'heeTag' and consequently empty ItemStack tag`(){
				val stack = ItemStack(Items.BOW).apply { heeTag }
				assertNotNull(stack.heeTagOrNull)
				
				stack.cleanupNBT()
				assertNull(stack.heeTagOrNull)
				assertNull(stack.nbtOrNull)
			}
			
			@Test fun `'cleanupNBT' correctly processes a tag with mixed data`(){
				val stack = ItemStack(Items.BOW).apply {
					nbt.put("a", TagCompound())
					nbt.put("b", TagCompound().also { it.put("bb", TagCompound()) })
					nbt.put("c", TagCompound().also { it.putString("key", "Hello"); it.put("cc", TagCompound()) })
				}
				
				assertEquals(3, stack.nbt.size())
				
				stack.cleanupNBT()
				assertEquals(1, stack.nbt.size())
				assertFalse(stack.nbt.hasKey("a"))
				assertFalse(stack.nbt.hasKey("b"))
				assertTrue(stack.nbt.hasKey("c"))
				assertFalse(stack.nbt.getCompound("c").hasKey("cc"))
				assertEquals("Hello", stack.nbt.getCompound("c").getString("key"))
			}
		}
	}
	
	@Nested inner class NBTPrimitiveLists{
		@Nested inner class Properties{
			@Test fun `'isEmpty" returns true for empty tags`(){
				val list = TagCompound().getListOfPrimitives("key")
				assertTrue(list.isEmpty)
			}
			
			@Test fun `'isEmpty' returns false for non-empty tags`(){
				val list = TagCompound().getListOfPrimitives("key")
				
				list.append(1)
				list.append(2)
				assertFalse(list.isEmpty)
			}
			
			@Test fun `'size' returns correct amount of tags`(){
				val list = TagCompound().getListOfPrimitives("key")
				
				assertEquals(0, list.size)
				list.append(5)
				assertEquals(1, list.size)
				list.append(5)
				assertEquals(2, list.size)
			}
		}
		
		@Nested inner class AppendGet{
			private inline fun <reified T : Any> testAppendGet(callAppend: (NBTPrimitiveList) -> Unit, callGetCheck: (T) -> Boolean){
				with(TagCompound()){
					val list = getListOfPrimitives("key")
					
					callAppend(list)
					putList("key", list)
					
					val primitive1 = (get("key") as TagList).get(0)
					assertTrue(primitive1 is T)
					assertTrue(callGetCheck(primitive1 as T))
					
					val primitive2 = list.get(0)
					assertTrue(primitive2 is T)
					assertTrue(callGetCheck(primitive2 as T))
				}
			}
			
			@Test fun `'append' using byte updates the tag and 'get' returns the same value`(){
				testAppendGet<TagByte>(
					{ list -> list.append(Byte.MAX_VALUE) },
					{ primitive -> primitive.byte == Byte.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using short updates the tag and 'get' returns the same value`(){
				testAppendGet<TagShort>(
					{ list -> list.append(Short.MAX_VALUE) },
					{ primitive -> primitive.short == Short.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using int updates the tag and 'get' returns the same value`(){
				testAppendGet<TagInt>(
					{ list -> list.append(Int.MAX_VALUE) },
					{ primitive -> primitive.int == Int.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using long updates the tag and 'get' returns the same value`(){
				testAppendGet<TagLong>({
					list -> list.append(Long.MAX_VALUE) },
					{ primitive -> primitive.long == Long.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using float updates the tag and 'get' returns the same value`(){
				testAppendGet<TagFloat>(
					{ list -> list.append(Float.MAX_VALUE) },
					{ primitive -> primitive.float == Float.MAX_VALUE }
				)
			}
			
			@Test fun `'append' using double updates the tag and 'get' returns the same value`(){
				testAppendGet<TagDouble>(
					{ list -> list.append(Double.MAX_VALUE) },
					{ primitive -> primitive.double == Double.MAX_VALUE }
				)
			}
		}
		
		@Nested inner class Get{
			private fun makeFilledTestList() = TagCompound().getListOfPrimitives("key").apply {
				append(1)
				append(2)
				append(3)
			}
			
			@Test fun `'get' returns correct value`(){
				val list = makeFilledTestList()
				assertEquals(TagInt.valueOf(1), list.get(0))
				assertEquals(TagInt.valueOf(2), list.get(1))
				assertEquals(TagInt.valueOf(3), list.get(2))
			}
			
			@Test fun `'get' throws when out of bounds`(){
				val list = makeFilledTestList()
				assertThrows<IndexOutOfBoundsException> { list.get(-1) }
				assertThrows<IndexOutOfBoundsException> { list.get(3) }
			}
		}
		
		@Nested inner class Iterators{
			private fun makeFilledTestList() = TagCompound().getListOfPrimitives("key").apply {
				append(1)
				append(2)
				append(3)
				append(Int.MIN_VALUE)
				append(Int.MAX_VALUE)
			}
			
			@Test fun `iterating goes through all items`(){
				val list = makeFilledTestList()
				assertIterableEquals(intArrayOf(1, 2, 3, Int.MIN_VALUE, Int.MAX_VALUE).map { TagInt.valueOf(it) }.asIterable(), list)
			}
			
			@Test fun `'hasNext' returns false and 'next' throws if empty`(){
				val list = TagCompound().getListOfPrimitives("key")
				val iterator = list.iterator()
				
				assertFalse(iterator.hasNext())
				assertThrows<NoSuchElementException> { iterator.next() }
			}
			
			@Test fun `'hasNext' returns true and 'next' returns value if non-empty`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertTrue(iterator.hasNext())
				assertEquals(TagInt.valueOf(1), iterator.next())
			}
			
			@Test fun `'remove' throws before calling 'next'`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertThrows<IllegalStateException> { iterator.remove() }
			}
			
			@Test fun `'remove' removes first element correctly`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertEquals(TagInt.valueOf(1), iterator.next())
				iterator.remove()
				assertEquals(TagInt.valueOf(2), iterator.next())
				
				assertIterableEquals(intArrayOf(2, 3, Int.MIN_VALUE, Int.MAX_VALUE).map { TagInt.valueOf(it) }.asIterable(), list)
			}
			
			@Test fun `'remove' removes several elements correctly`(){
				val list = makeFilledTestList()
				val iterator = list.iterator()
				
				assertEquals(TagInt.valueOf(1), iterator.next())
				assertEquals(TagInt.valueOf(2), iterator.next())
				iterator.remove()
				assertEquals(TagInt.valueOf(3), iterator.next())
				assertEquals(TagInt.valueOf(Int.MIN_VALUE), iterator.next())
				iterator.remove()
				assertEquals(TagInt.valueOf(Int.MAX_VALUE), iterator.next())
				
				assertIterableEquals(intArrayOf(1, 3, Int.MAX_VALUE).map { TagInt.valueOf(it) }.asIterable(), list)
			}
		}
		
		@Nested inner class SequenceGetters{
			private inline fun <T : Any> testSequenceGetter(callAppend: (NBTPrimitiveList, T) -> Unit, callGetSequence: (NBTPrimitiveList) -> Iterable<T>, testValues: Array<T>){
				with(TagCompound()){
					val list = getListOfPrimitives("key")
					
					for(element in testValues){
						callAppend(list, element)
					}
					
					assertIterableEquals(testValues.asIterable(), callGetSequence(list))
				}
			}
			
			@Test fun `'allBytes' retrieves all values`(){
				testSequenceGetter(
					{ list, item -> list.append(item) },
					NBTPrimitiveList::allBytes,
					arrayOf(1.toByte(), 2.toByte(), 3.toByte(), Byte.MIN_VALUE, Byte.MAX_VALUE)
				)
			}
			
			@Test fun `'allShorts' retrieves all values`(){
				testSequenceGetter(
					{ list, item -> list.append(item) },
					NBTPrimitiveList::allShorts,
					arrayOf(1.toShort(), 2.toShort(), 3.toShort(), Short.MIN_VALUE, Short.MAX_VALUE)
				)
			}
			
			@Test fun `'allInts' retrieves all values`(){
				testSequenceGetter(
					{ list, item -> list.append(item) },
					NBTPrimitiveList::allInts,
					arrayOf(1, 2, 3, Int.MIN_VALUE, Int.MAX_VALUE)
				)
			}
			
			@Test fun `'allLongs' retrieves all values`(){
				testSequenceGetter(
					{ list, item -> list.append(item) },
					NBTPrimitiveList::allLongs,
					arrayOf(1L, 2L, 3L, Long.MIN_VALUE, Long.MAX_VALUE)
				)
			}
			
			@Test fun `'allFloats' retrieves all values`(){
				testSequenceGetter(
					{ list, item -> list.append(item) },
					NBTPrimitiveList::allFloats,
					arrayOf(1F, 2F, 3F, Float.MIN_VALUE, Float.MAX_VALUE)
				)
			}
			
			@Test fun `'allDoubles' retrieves all values`(){
				testSequenceGetter(
					{ list, item -> list.append(item) },
					NBTPrimitiveList::allDoubles,
					arrayOf(1.0, 2.0, 3.0, Double.MIN_VALUE, Double.MAX_VALUE)
				)
			}
		}
	}
	
	@Nested inner class NBTObjectLists{
		@Nested inner class Properties{
			@Test fun `'isEmpty' returns true for empty tags`(){
				val list2 = TagCompound().getListOfStrings("key")
				assertTrue(list2.isEmpty)
			}
			
			@Test fun `'isEmpty' returns false for non-empty tags`(){
				val list2 = TagCompound().getListOfStrings("key")
				
				list2.append("test")
				assertFalse(list2.isEmpty)
			}
			
			@Test fun `'size' returns correct amount of tags`(){
				val list2 = TagCompound().getListOfStrings("key")
				
				assertEquals(0, list2.size)
				list2.append("first")
				assertEquals(1, list2.size)
				list2.append("second")
				assertEquals(2, list2.size)
			}
		}
		
		@Nested inner class AppendGet{
			private inline fun <T : Any> testAppendGet(listGetter: TagCompound.(String) -> NBTObjectList<T>, callAppend: (NBTObjectList<T>) -> Unit, callGetCheck: (T) -> Boolean){
				with(TagCompound()){
					val list = listGetter("key")
					
					callAppend(list)
					putList("key", list)
					
					assertTrue((get("key") as TagList).get(0) is NBTBase)
					assertTrue(callGetCheck(list.get(0)))
				}
			}
			
			@Test fun `'append' using TagCompound updates the tag and 'get' returns the same value`(){
				testAppendGet(
					TagCompound::getListOfCompounds,
					{ list -> list.append(TagCompound().apply { putInt("test", 123) }) },
					{ value -> value.getInt("test") == 123 }
				)
			}
			
			@Test fun `'append' using String updates the tag and 'get' returns the same value`(){
				testAppendGet(
					TagCompound::getListOfStrings,
					{ list -> list.append("hello") },
					{ value -> value == "hello" }
				)
			}
			
			@Test fun `'append' using ByteArray updates the tag and 'get' returns the same value`(){
				testAppendGet(
					TagCompound::getListOfByteArrays,
					{ list -> list.append(byteArrayOf(1, 2, 3)) },
					{ value -> value.contentEquals(byteArrayOf(1, 2, 3)) }
				)
			}
			
			@Test fun `'append' using IntArray updates the tag and 'get' returns the same value`(){
				testAppendGet(
					TagCompound::getListOfIntArrays,
					{ list -> list.append(intArrayOf(1, 2, 3)) },
					{ value -> value.contentEquals(intArrayOf(1, 2, 3)) }
				)
			}
		}
		
		@Nested inner class Get{
			private fun makeFilledTestList() = TagCompound().getListOfStrings("key").apply {
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
			private fun makeFilledTestList() = TagCompound().getListOfStrings("key").apply {
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
				val list = TagCompound().getListOfPrimitives("key")
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
