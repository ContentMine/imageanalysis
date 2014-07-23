=== ImageAnalysis ===

Basic routines for processing scientific and similar diagrams. Primarily works on clear primitives shapes (lines,
symbols, curves, polygons, clear text).

== Architecture ==

Primarily now based on BoofCV as the initial processor. Line detection mainly now through thinning, floodfill and
analysis of PixelIslands.

= PixelIsland =

A set of contiguous pixels ( could enclose othr islands recursively) . Most routines assume that touching can be through
diagonal pixels. There is a flag for this but it's not systematic. Many islands have been thinned and therefore consist
of a graph of PixelEdges and PixelNodes (subclassed as TerminalNode and NucleusNode). (We had a lot of struggle with 
different type of JunctionNode and are gradually abandong this - it depends on the thhhing success).

== PixelGraph ==
Routines which extract the topology of the pixels


