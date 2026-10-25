# Directrices de Desarrollo: The Void Plague (NeoForge 1.21.1)

## 1. Stack Técnico Obligatorio
- **Minecraft:** 1.21.1
- **Mod Loader:** NeoForge (versión recomendada reciente para 1.21.1)
- **Java:** JDK 21
- **Mappings:** Mojang Official (Mojmap)
- **ID del Mod:** `void_plague`
- **Paquete Base:** `com.voidplague.mod`

## 2. Reglas de API Estrictas (1.21.1)
- **Prohibido el uso de NBT tradicional en Items:** Minecraft 1.21 eliminó el tag NBT directo en `ItemStack`. Utiliza exclusivamente `Data Components` (`DataComponentType`).
- **Registros:** Utiliza siempre `DeferredRegister` (ej. `DeferredRegister.Blocks`, `DeferredRegister.Items`) asignados al bus de eventos de ciclo de vida del mod (`IEventBus`).
- **Networking:** Utiliza el nuevo sistema de Payloads (`CustomPacketPayload`) y registra canales mediante el evento de red de NeoForge (`PayloadRegistrar`).
- **DataGen:** Todos los recursos (JSONs de modelos, blockstates, tablas de botín, recetas y tags) deben generarse programáticamente mediante el `GatherDataEvent`. No crees archivos JSON a mano salvo que sea estrictamente necesario.
- **Mixins:** Solo permitidos si no existe un evento de NeoForge para esa función. Cualquier Mixin debe documentarse con el motivo exacto de la inyección.

## 3. Comportamiento del Agente
- No des por finalizada una tarea sin verificar la compilación mediante `./gradlew build -x test`.
- Si un comando de Gradle falla, lee el stacktrace completo, identifica el método deprecado/inexistente y corrige la clase afectada antes de continuar.
- Mantén la modularidad por paquetes: `.core`, `.registry`, `.world`, `.entity`, `.client`.