package chylex.hee.client.text

sealed class LocalizationStrategy {
	abstract fun localize(key: String): String?
	
	object Default : LocalizationStrategy() {
		private val nonCapitalizedWords = setOf("of", "and")
		
		override fun localize(key: String): String {
			return key.split('_').joinToString(" ") {
				if (nonCapitalizedWords.contains(it)) it else it.replaceFirstChar(Char::titlecaseChar)
			}
		}
	}
	
	open class ReplaceWords(private val wrapped: LocalizationStrategy, private val toReplace: String, private val replacement: String?) : LocalizationStrategy() {
		constructor(toReplace: String, replacement: String?) : this(Default, toReplace, replacement)
		
		override fun localize(key: String): String? {
			val localized = wrapped.localize(key) ?: return null
			val words = localized.split(' ').toMutableList()
			val toDelete = this.toReplace.split(' ')
			
			for (index in words.indices) {
				if (toDelete.withIndex().all { (offset, word) -> words[index + offset] == word }) {
					words.subList(index, index + toDelete.size).clear()
					replacement?.let { words.add(index, it) }
					break
				}
			}
			
			return words.joinToString(" ")
		}
	}
	
	class DeleteWords(wrapped: LocalizationStrategy, toDelete: String) : ReplaceWords(wrapped, toDelete, null) {
		constructor(toDelete: String) : this(Default, toDelete)
	}
	
	class Parenthesized(private val wrapped: LocalizationStrategy = Default, private val wordCount: Int, private val wordOffset: Int = 0, private val fromStart: Boolean = false) : LocalizationStrategy() {
		override fun localize(key: String): String? {
			val localized = wrapped.localize(key) ?: return null
			val words = localized.split(' ').toMutableList()
			
			val firstIndex = if (fromStart) wordOffset else words.size - wordOffset - wordCount
			val lastIndex = firstIndex + wordCount - 1
			if (firstIndex < 0 || lastIndex > words.lastIndex) {
				return localized
			}
			
			words[firstIndex] = '(' + words[firstIndex]
			words[lastIndex] = words[lastIndex] + ')'
			
			return words.joinToString(" ")
		}
	}
	
	class MoveToBeginning(private val wrapped: LocalizationStrategy = Default, private val wordCount: Int, private val wordOffset: Int = 0, private val fromStart: Boolean = false) : LocalizationStrategy() {
		override fun localize(key: String): String? {
			val localized = wrapped.localize(key) ?: return null
			val words = localized.split(' ').toMutableList()
			
			val firstIndex = if (fromStart) wordOffset else words.size - wordOffset - wordCount
			if (firstIndex < 0 || firstIndex + wordCount > words.size) {
				return localized
			}
			
			val subList = words.subList(firstIndex, firstIndex + wordCount)
			val movedWords = subList.toList()
			
			subList.clear()
			words.addAll(0, movedWords)
			return words.joinToString(" ")
		}
	}
	
	class Custom(private val localized: String) : LocalizationStrategy() {
		override fun localize(key: String): String {
			return localized
		}
	}
	
	object None : LocalizationStrategy() {
		override fun localize(key: String): String? {
			return null
		}
	}
}
