# Mecha Duel

Proyecto 2 de Programación Avanzada (Pontificia Universidad Javeriana, 2026-30).

Combate por turnos entre dos robots (mechas), jugado en red entre dos computadores por sockets y con interfaz gráfica en JavaFX con estilo pixel art.

## Requisitos

- JDK 25
- Maven (incluido con el wrapper `mvnw`, no hace falta instalarlo)

## Cómo ejecutar

Desde la carpeta del proyecto:

```powershell
.\mvnw javafx:run
```

En IntelliJ: panel **Maven → Plugins → javafx → javafx:run**.

### Probar en un solo computador

Abre el juego dos veces y conecta las ventanas así:

| Ventana | Mi puerto | IP del rival | Puerto del rival |
|---------|-----------|--------------|------------------|
| 1       | 5000      | 127.0.0.1    | 5001             |
| 2       | 5001      | 127.0.0.1    | 5000             |

En dos computadores distintos, cada uno escribe la IP del otro.

## Reglas

- Estadísticas: `ATK + DEF + SPD + NRG = 200`. Todos los robots empiezan con `HP = 100`.
- Ataque normal: `D = ATK² / (ATK + DEF)`.
- Ataque especial: `D = (ATK + 0.5·NRG)² / (ATK + 0.5·NRG + DEF)`. Después la energía vuelve a 0.
- Turnos: cada robot suma su SPD a un acumulador; al llegar a 100 juega y se le restan 100.
- Movimientos: `ATTACK`, `SPECIAL`, `DODGE` (50 % de esquivar, 75 % si se usa dos veces), `HEAL` (+10 HP) y `CHARGE` (+10 NRG).
- Efectos de estado (uno por robot): Quemadura `Q` (−3 HP al rival), Regeneración `R` (+3 HP) y Núcleo Activo `N` (+2 NRG). Se aplican al inicio de cada turno.

## Flujo del juego

1. **Conexión:** se ingresan la IP y el puerto del rival.
2. **Ensamble:** se elige el mecha, se reparten las estadísticas y se envían al rival.
3. **Estrategia:** con archivo (`estrategia.txt`, que se repite en ciclo) o a mano. Si no hay archivo, se va creando durante el combate.
4. **Combate:** cada movimiento se envía por socket; los ataques reciben `HIT` o `DODGE`.
5. **Fin:** se puede guardar el robot en `robots.bin` y los movimientos de la partida para la próxima estrategia.

## Protocolo de red

| Mensaje     | Formato                                                     |
|-------------|-------------------------------------------------------------|
| Robot       | `{HP:100, ATK:60, DEF:50, SPD:50, NRG:40, EFF:[Q]}`         |
| Movimiento  | `ATTACK` \| `SPECIAL` \| `DODGE` \| `HEAL` \| `CHARGE`      |
| Respuesta   | `HIT` \| `DODGE`                                            |

## Estructura del código

Todo está en `src/main/java/com/javeriana/proyecto/robotduel`:

| Paquete    | Contenido |
|------------|-----------|
| `modelo`   | `Robot`, `EstadoCombate`, la jerarquía de movimientos (`Movimiento` → `MovimientoOfensivo` → `Ataque`, `Especial`; `Curar`, `Cargar`, `Esquivar`) y los efectos (`Efecto` → `Quemadura`, `Regeneracion`, `NucleoActivo`). |
| `logica`   | `Combate`, `Turnos`, las fuentes de movimientos (`EstrategiaArchivo`, `EstrategiaManual`) y la resolución de impactos. |
| `archivos` | `GestorEstrategia` (`estrategia.txt`), `GestorRobots` (`robots.bin`, serialización) y `RegistroPartida`. |
| `red`      | `Conexion` (sockets de entrada y salida), `Protocolo`, `PartidaEnRed`, `JugadorRemoto`, `FuenteLocalEnRed` e `ImpactoEnRed`. |
| `vista`    | `MechaDuelApp` y las pantallas de conexión, ensamble, estrategia y combate, con los componentes pixel art. |

## Ramas

- `master`: versión de entrega.
- `develop`: integración.
- `modelo`, `logica`: ramas de trabajo que se integran a `develop` con Pull Requests.
