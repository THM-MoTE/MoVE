# Modelica Vector graphics Editor (Move)

Move is a __cross-plattform__ graphical editor for __Modelica's graphical objects__. With Move you can create your Modelica-Models in a few minutes. Move has a handy, beautiful UI.

Move supports all basic editing you need or miss in similar projects:
- Draw __straight__ lines (either horizontal or vertical),  __real__ circles, squares, polygons and much more
- Resize the shapes in __4 regions__
- Move shapes around
- import your favourite bitmaps in nearly every image format (jpeg, jpg, png, bmp, gif) either as URL or encoded in base64
- Layout elements at a grid with __snap-to-grid__ (can get disabled)
- undo and redo your last few editings
- generate either __formatted__ or __one-line__ Modelica code
- simple shortcuts that can be remembered or changed if you prefer others

__Move is fast!__ Nearly every feature works in real time!

## Get a taste
### Drawing shapes
#### Version 0.1 on Ubuntu
![Example drawings v0.1](doc/move-v0.1.png)

#### Version 0.5 on Mac OS X
![Example drawings v0.2](doc/move-v0.5.png)

### Generated formatted Modelica code
``` modelica
model test
  annotation(
  Icon (
    coordinateSystem(
      extent = {{0,0},{754,497}}
    ),
    graphics = {
      Ellipse(
        lineColor = {0,0,0},
        fillColor = {26,51,153},
        fillPattern = FillPattern.Solid,
        lineThickness = 4.0,
        extent = {{110,290},{288,112}},
        endAngle = 360
      ),
      Polygon(
        points = {{491,230},{543,269},{593,237},{612,301},{518,336},{433,269}},
        lineColor = {255,128,128},
        fillColor = {77,128,77},
        fillPattern = FillPattern.Solid,
        lineThickness = 4.0
      ),
      Rectangle(
        lineColor = {26,51,153},
        fillColor = {230,230,77},
        fillPattern = FillPattern.Solid,
        lineThickness = 1.0,
        extent = {{70,401}, {335,358}}
      ),
      Line(
        points = {{250,460},{541,460}},
        color = {179,26,26},
        thickness = 8.0
      )
    })
  );
end test;
```

# Try it
If you like to try it follow these steps:
- Make sure you are using JDK version >= __1.8.0_77__ by running ```java -version```
(Because of a bug in the JavaFX lib that causes strange
  behaviour from the JavaFX-FileChooser we need JDK version >= 1.8.0_77)
- Install __sbt__ for building move from source
- Clone this repository
- Get into the target directory
- Compile and run Move: ```sbt ";compile;run"```

# License
We __didn't pick__ a license yet! All copyright belongs to the developers.
