package chylex.hee.game.command.argument;

final class EnumArgumentConstructor {
	private EnumArgumentConstructor() {}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static EnumArgument<?> createEnumArgument(final Class<?> cls) {
		if (!cls.isEnum()) {
			throw new IllegalArgumentException("cannot create an EnumArgument for class: " + cls.getName());
		}
		
		return new EnumArgument(cls);
	}
}
