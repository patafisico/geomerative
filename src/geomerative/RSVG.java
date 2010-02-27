/**
    Copyright 2004-2008 Ricard Marxer  <email@ricardmarxer.com>

    This file is part of Geomerative.

    Geomerative is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Geomerative is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Geomerative.  If not, see <http://www.gnu.org/licenses/>.
*/

package geomerative;

import processing.core.*;
import processing.xml.*;

/**
 * @extended
 */
public class RSVG
{
  public void draw(String filename, PGraphics g)
  {
    this.toGroup(filename).draw(g);
  }

  public void draw(String filename, PApplet p)
  {
    this.toGroup(filename).draw(p);
  }

  public void draw(String filename)
  {
    this.toGroup(filename).draw();
  }

  public void saveShape(String filename, RShape shp) {
    String str = fromShape(shp);
    String[] strs = PApplet.split(str, "\n");
    RG.parent().saveStrings(filename, strs);
  }

  public String fromShape(RShape shape) {
    String header = "<?xml version=\"1.0\" standalone=\"no\"?>\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n<svg width=\"100%\" height=\"100%\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\n";

    return header + shapeToString(shape) + "</svg>";
  }

  public void saveGroup(String filename, RGroup grp) {
    String str = fromGroup(grp);
    String[] strs = PApplet.split(str, "\n");
    RG.parent().saveStrings(filename, strs);
  }

  public String fromGroup(RGroup group) {
    String header = "<?xml version=\"1.0\" standalone=\"no\"?>\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n<svg width=\"100%\" height=\"100%\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\n";

    return header + groupToString(group) + "</svg>";
  }

  

  public RGroup toGroup(String filename)
  {
    XMLElement svg = new XMLElement(RG.parent(), filename);
    if (!svg.getName().equals("svg")) {
      throw new RuntimeException("root is not <svg>, it's <" + svg.getName() + ">");
    }

    return elemToGroup(svg);
  }

  public RShape toShape(String filename)
  {
    XMLElement svg = new XMLElement(RG.parent(), filename);
    if (!svg.getName().equals("svg")) {
      throw new RuntimeException("root is not <svg>, it's <" + svg.getName() + ">");
    }

    RShape result = elemToCompositeShape(svg);
    result.width = result.getWidth();
    result.height = result.getHeight();

    return result;
  }

  public RPolygon toPolygon(String filename)
  {
    return toGroup(filename).toPolygon();
  }

  public RMesh toMesh(String filename)
  {
    return toGroup(filename).toMesh();
  }

  public String groupToString(RGroup grp) {
    String result = "";
    result += "<g ";
    result += styleToString(grp.getStyle());
    result += ">\n";

    for(int i=0;i<grp.countElements();i++) {
      switch(grp.elements[i].getType()){
      case RGeomElem.GROUP:
        result += groupToString((RGroup)grp.elements[i]);
        break;

      case RGeomElem.POLYGON:
        result += polygonToString((RPolygon)grp.elements[i]);
        break;

      case RGeomElem.SHAPE:
        result += shapeToString((RShape)grp.elements[i]);
        break;

      }
    }

    result += "</g>\n";
    return result;
  }

  public String polygonToString(RPolygon poly) {
    String result = "";
    result += shapeToString(poly.toShape());
    return result;
  }

  public String shapeToString(RShape shp) {
    String result = "";

    // If it has children it is a group
    result += "<g ";
    result += styleToString(shp.getStyle());
    result += ">\n";

    if (shp.countPaths() > 0) {
      result += "<path ";
      result += "d=\"";

      for(int i=0; i<shp.countPaths(); i++) {
        RPath sushp = shp.paths[i];
        boolean init = true;
        for ( int j = 0; j < sushp.countCommands(); j++ ) {
          RCommand cmd = sushp.commands[j];

          if (init) {
            result += "M" + cmd.startPoint.x + " " + cmd.startPoint.y + " ";
            init = false;
          }

          switch( cmd.getCommandType() )
            {
            case RCommand.LINETO:
              result += "L" + cmd.endPoint.x + " " + cmd.endPoint.y + " ";
              break;

            case RCommand.QUADBEZIERTO:
              result += "Q" + cmd.controlPoints[0].x + " " + cmd.controlPoints[0].y + cmd.endPoint.x + " " + cmd.endPoint.y + " ";
              break;

            case RCommand.CUBICBEZIERTO:
              result += "C" + cmd.controlPoints[0].x + " " + cmd.controlPoints[0].y + " " + cmd.controlPoints[1].x + " " + cmd.controlPoints[1].y + " " + cmd.endPoint.x + " " + cmd.endPoint.y + " ";
              break;
            }
        }

        if (sushp.closed) {
          result += "Z ";
        }
      }

      result += "\"/>\n";
    }

    for (int i=0; i<shp.countChildren(); i++) {
      result+=shapeToString(shp.children[i]);
    }

    result += "</g>\n";
    return result;
  }

  public String styleToString(RStyle style) {
    String result = " style=\"";

    if (style.fillDef) {
      if (!style.fill) {
        result += "fill:none;";
      } else {
        result += "fill:#" + PApplet.hex(style.fillColor, 6) + ";";
      }
    }

    if (style.fillAlphaDef) {
      result += "fill-opacity:" + style.fillAlpha/255.0f + ";";
    }

    if (style.strokeDef) {
      if (!style.stroke) {
        result += "stroke:none;";
      } else {
        result += "stroke:#" + PApplet.hex(style.strokeColor, 6) + ";";
      }
    }

    if (style.strokeAlphaDef) {
      result += "stroke-opacity:" + style.strokeAlpha/255.0f + ";";
    }

    if (style.strokeWeightDef) {
      result += "stroke-width:" + style.strokeWeight + ";";
    }


    if(style.strokeCapDef) {
      result += "stroke-linecap:";

      switch (style.strokeCap) {
      case RG.PROJECT:
        result += "butt";
        break;
      case RG.ROUND:
        result += "round";
        break;
      case RG.SQUARE:
        result += "square";
        break;

      default:
        break;
      }

      result += ";";
    }

    if(style.strokeJoinDef) {
      result += "stroke-linejoin:";

      switch (style.strokeJoin) {
      case RG.MITER:
        result += "miter";
        break;
      case RG.ROUND:
        result += "round";
        break;
      case RG.BEVEL:
        result += "bevel";
        break;

      default:
        break;
      }

      result += ";";
    }

    result += "\" ";
    return result;
  }

  /**
   * @invisible
   */
  public RGroup elemToGroup(XMLElement elem)
  {
    RGroup grp = new RGroup();

    // Set the defaults SVG styles for the root
    if(elem.getName().toLowerCase().equals("svg")){
      grp.setFill(0);  // By default in SVG it's black
      grp.setFillAlpha(255);  // By default in SVG it's 1
      grp.setStroke(false);  // By default in SVG it's none
      grp.setStrokeWeight(1F);  // By default in SVG it's none
      grp.setStrokeCap("butt");  // By default in SVG it's 'butt'
      grp.setStrokeJoin("miter");  // By default in SVG it's 'miter'
      grp.setStrokeAlpha(255);  // By default in SVG it's 1
      grp.setAlpha(255);  // By default in SVG it's 1F
    }

    XMLElement elems[] = elem.getChildren();
    for (int i = 0; i < elems.length; i++) {
      String name = elems[i].getName().toLowerCase();
      XMLElement element = elems[i];

      // Parse and create the geometrical element
      RGeomElem geomElem = null;
      if(name.equals("g")){
        geomElem = elemToGroup(element);

      }else if (name.equals("path")) {
        geomElem = elemToShape(element);

      }else if(name.equals("polygon")){
        geomElem = elemToPolygon(element);

      }else if(name.equals("polyline")){
        geomElem = elemToPolyline(element);

      }else if(name.equals("circle")){
        geomElem = elemToCircle(element);

      }else if(name.equals("ellipse")){
        geomElem = elemToEllipse(element);

      }else if(name.equals("rect")){
        geomElem = elemToRect(element);

      }else if(name.equals("line")){
        geomElem = elemToLine(element);

      }else if(name.equals("defs")){
        // Do nothing normally we should make a hashmap
        // to apply everytime they are called in the actual objects
      }else{
        PApplet.println("Element '" + name + "' not know. Ignoring it.");
      }

      // If the geometrical element has been correctly created
      if((geomElem != null)){
        // Transform geometrical element
        if(element.hasAttribute("transform")){
          String transformString = element.getStringAttribute("transform");
          RMatrix transf = new RMatrix(transformString);
          geomElem.transform(transf);
        }

        // Get the id for the geometrical element
        if(element.hasAttribute("id")){
          geomElem.name = element.getStringAttribute("id");
        }

        // Get the style for the geometrical element
        if(element.hasAttribute("style")){
          geomElem.setStyle(element.getStringAttribute("style"));
        }

        // Get the fill for the geometrical element
        if(element.hasAttribute("fill")){
          geomElem.setFill(element.getStringAttribute("fill"));
        }

        // Get the fill-linejoin for the geometrical element
        if(element.hasAttribute("fill-opacity")){
          geomElem.setFillAlpha(element.getStringAttribute("fill-opacity"));
        }

        // Get the stroke for the geometrical element
        if(element.hasAttribute("stroke")){
          geomElem.setStroke(element.getStringAttribute("stroke"));
        }

        // Get the stroke-width for the geometrical element
        if(element.hasAttribute("stroke-width")){
          geomElem.setStrokeWeight(element.getStringAttribute("stroke-width"));
        }

        // Get the stroke-linecap for the geometrical element
        if(element.hasAttribute("stroke-linecap")){
          geomElem.setStrokeCap(element.getStringAttribute("stroke-linecap"));
        }

        // Get the stroke-linejoin for the geometrical element
        if(element.hasAttribute("stroke-linejoin")){
          geomElem.setStrokeJoin(element.getStringAttribute("stroke-linejoin"));
        }

        // Get the stroke-linejoin for the geometrical element
        if(element.hasAttribute("stroke-opacity")){
          geomElem.setStrokeAlpha(element.getStringAttribute("stroke-opacity"));
        }

        // Get the opacity for the geometrical element
        if(element.hasAttribute("opacity")){
          geomElem.setAlpha(element.getStringAttribute("opacity"));
        }

        // Get the style for the geometrical element
        grp.addElement(geomElem);
      }
    }

    // Set the original width and height
    grp.updateOrigParams();

    return grp;
  }

  /**
   * @invisible
   */
  public RShape elemToCompositeShape(XMLElement elem)
  {
    RShape shp = new RShape();

    // Set the defaults SVG styles for the root
    if(elem.getName().toLowerCase().equals("svg")){
      shp.setFill(0);  // By default in SVG it's black
      shp.setFillAlpha(255);  // By default in SVG it's 1
      shp.setStroke(false);  // By default in SVG it's none
      shp.setStrokeWeight(1F);  // By default in SVG it's none
      shp.setStrokeCap("butt");  // By default in SVG it's 'butt'
      shp.setStrokeJoin("miter");  // By default in SVG it's 'miter'
      shp.setStrokeAlpha(255);  // By default in SVG it's 1
      shp.setAlpha(255);  // By default in SVG it's 1F
    }

    XMLElement elems[] = elem.getChildren();
    for (int i = 0; i < elems.length; i++) {
      String name = elems[i].getName().toLowerCase();
      XMLElement element = elems[i];

      // Parse and create the geometrical element
      RShape geomElem = null;
      if(name.equals("g")){
        geomElem = elemToCompositeShape(element);

      }else if (name.equals("path")) {
        geomElem = elemToShape(element);

      }else if(name.equals("polygon")){
        geomElem = elemToPolygon(element);

      }else if(name.equals("polyline")){
        geomElem = elemToPolyline(element);

      }else if(name.equals("circle")){
        geomElem = elemToCircle(element);

      }else if(name.equals("ellipse")){
        geomElem = elemToEllipse(element);

      }else if(name.equals("rect")){
        geomElem = elemToRect(element);

      }else if(name.equals("line")){
        geomElem = elemToLine(element);

      }else if(name.equals("defs")){
        // Do nothing normally we should make a hashmap
        // to apply everytime they are called in the actual objects
      }else{
        PApplet.println("Element '" + name + "' not know. Ignoring it.");
      }

      // If the geometrical element has been correctly created
      if((geomElem != null)){
        // Transform geometrical element
        if(element.hasAttribute("transform")){
          String transformString = element.getStringAttribute("transform");
          RMatrix transf = new RMatrix(transformString);
          geomElem.transform(transf);
        }

        // Get the id for the geometrical element
        if(element.hasAttribute("id")){
          geomElem.name = element.getStringAttribute("id");
        }

        // Get the style for the geometrical element
        if(element.hasAttribute("style")){
          geomElem.setStyle(element.getStringAttribute("style"));
        }

        // Get the fill for the geometrical element
        if(element.hasAttribute("fill")){
          geomElem.setFill(element.getStringAttribute("fill"));
        }

        // Get the fill-linejoin for the geometrical element
        if(element.hasAttribute("fill-opacity")){
          geomElem.setFillAlpha(element.getStringAttribute("fill-opacity"));
        }

        // Get the stroke for the geometrical element
        if(element.hasAttribute("stroke")){
          geomElem.setStroke(element.getStringAttribute("stroke"));
        }

        // Get the stroke-width for the geometrical element
        if(element.hasAttribute("stroke-width")){
          geomElem.setStrokeWeight(element.getStringAttribute("stroke-width"));
        }

        // Get the stroke-linecap for the geometrical element
        if(element.hasAttribute("stroke-linecap")){
          geomElem.setStrokeCap(element.getStringAttribute("stroke-linecap"));
        }

        // Get the stroke-linejoin for the geometrical element
        if(element.hasAttribute("stroke-linejoin")){
          geomElem.setStrokeJoin(element.getStringAttribute("stroke-linejoin"));
        }

        // Get the stroke-linejoin for the geometrical element
        if(element.hasAttribute("stroke-opacity")){
          geomElem.setStrokeAlpha(element.getStringAttribute("stroke-opacity"));
        }

        // Get the opacity for the geometrical element
        if(element.hasAttribute("opacity")){
          geomElem.setAlpha(element.getStringAttribute("opacity"));
        }

        // Get the style for the geometrical element
        shp.addChild(geomElem);
      }
    }

    shp.updateOrigParams();

    return shp;
  }

  /**
   * @invisible
   */
  public RShape elemToPolyline(XMLElement elem)
  {
    RShape shp = getPolyline(elem.getStringAttribute("points").trim());

    shp.updateOrigParams();

    return shp;
  }

  /**
   * @invisible
   */
  public RShape elemToPolygon(XMLElement elem)
  {
    RShape poly = elemToPolyline(elem);

    poly.addClose();

    poly.updateOrigParams();

    return poly;
  }

  /**
   * @invisible
   */
  public RShape elemToRect(XMLElement elem)
  {

    RShape shp = getRect(elem.getFloatAttribute("x"), elem.getFloatAttribute("y"), elem.getFloatAttribute("width"), elem.getFloatAttribute("height"));

    shp.updateOrigParams();

    return shp;
  }

  /**
   * @invisible
   */
  public RShape elemToLine(XMLElement elem)
  {
    RShape shp = getLine(elem.getFloatAttribute("x1"), elem.getFloatAttribute("y1"), elem.getFloatAttribute("x2"), elem.getFloatAttribute("y2"));

    shp.updateOrigParams();

    return shp;
  }


  /**
   * @invisible
   */
  public RShape elemToEllipse(XMLElement elem)
  {
    RShape shp = getEllipse(elem.getFloatAttribute("cx"), elem.getFloatAttribute("cy"), elem.getFloatAttribute("rx"), elem.getFloatAttribute("ry"));

    shp.updateOrigParams();

    return shp;
  }


  /**
   * @invisible
   */
  public RShape elemToCircle(XMLElement elem)
  {
    float r = elem.getFloatAttribute("r");
    RShape shp = getEllipse(elem.getFloatAttribute("cx"), elem.getFloatAttribute("cy"), r, r);

    shp.updateOrigParams();

    return shp;
  }

  /**
   * @invisible
   */
  public RShape elemToShape(XMLElement elem)
  {
    RShape shp = getShape(elem.getStringAttribute("d"));

    shp.updateOrigParams();

    return shp;
  }

  /**
   * @invisible
   */
  private RShape getRect(float x, float y, float w, float h)
  {
    RShape shp = RShape.createRectangle(x, y, w, h);

    shp.updateOrigParams();

    return shp;
  }

  /**
   * @invisible
   */
  private RShape getLine(float x1, float y1, float x2, float y2)
  {
    RShape shp = new RShape();

    shp.addMoveTo(x1, y1);
    shp.addLineTo(x2, y2);

    return shp;
  }


  /**
   * @invisible
   */
  private RShape getEllipse(float cx, float cy, float rx, float ry)
  {
    // RShape createEllipse takes as input the width and height of the ellipses
    return RShape.createEllipse(cx, cy, rx*2F, ry*2F);
  }

  /**
   * @invisible
   */
  private RShape getPolyline(String s)
  {
    RShape poly = new RShape();
    boolean first = true;

    //format string to usable format
    char charline[]=s.toCharArray();
    for(int i=0;i<charline.length;i++)
      {
        switch(charline[i])
          {
          case '-':
            if(charline[i-1] != 'e' && charline[i-1] != 'E'){
              charline=PApplet.splice(charline,' ',i);
              i++;
            }
            break;
          case ',':
          case '\n':
          case '\r':
          case '\t':
            charline[i]=' ';
            break;
          }
      }
    String formatted=new String(charline);
    String tags[]=PApplet.splitTokens(formatted,", ");
    for(int i=0;i<tags.length;i++){
      float x = PApplet.parseFloat(tags[i]);
      float y = PApplet.parseFloat(tags[i+1]);
      i++;
      if(first){
        poly.addMoveTo(x,y);
        first = false;
      }else{
        poly.addLineTo(x,y);
      }
    }
    return poly;
  }

  /**
   * @invisible
   */
  private RShape getShape(String s)
  {
    RShape shp = new RShape();

    if(s == null){
      return shp;
    }

    //format string to usable format
    char charline[] = s.toCharArray();
    for( int i = 0 ; i < charline.length ; i++)
      {
        switch(charline[i])
          {
          case 'M':
          case 'm':
          case 'Z':
          case 'z':
          case 'C':
          case 'c':
          case 'S':
          case 's':
          case 'L':
          case 'l':
          case 'H':
          case 'h':
          case 'V':
          case 'v':
            charline = PApplet.splice(charline,' ',i);
            i ++;
            charline = PApplet.splice(charline,' ',i+1);
            i ++;
            break;

          case '-':
            if(charline[i-1] != 'e' && charline[i-1] != 'E'){
              charline=PApplet.splice(charline,' ',i);
              i++;
            }
            break;
          case ',':
          case '\n':
          case '\r':
          case '\t':
            charline[i] = ' ';
            break;

          }
      }
    String formatted = new String(charline);
    String[] tags = PApplet.splitTokens(formatted);

    //PApplet.println("formatted: " + formatted);
    //PApplet.println("tags: ");
    //PApplet.println(tags);

    //build points
    RPoint curp = new RPoint();
    RPoint relp = new RPoint();
    RPoint refp = new RPoint();
    RPoint strp = new RPoint();

    char command = 'a';

    for(int i=0;i<tags.length;i++)
      {

        char nextChar = tags[i].charAt(0);
        switch(nextChar)
          {
          case 'm':
          case 'M':
          case 'c':
          case 'C':
          case 's':
          case 'S':
          case 'l':
          case 'L':
          case 'h':
          case 'H':
          case 'v':
          case 'V':
            i += 1;
          case 'z':
          case 'Z':
            command = nextChar;
            break;
        }

        relp.setLocation(0F, 0F);

        switch(command)
          {
          case 'm':
            relp.setLocation(curp.x, curp.y);
          case 'M':
            i = move(shp, curp, relp, refp, strp, tags, i);
            break;

          case 'z':
            relp.setLocation(curp.x, curp.y);
          case 'Z':
            shp.addClose();
            break;

          case 'c':
            relp.setLocation(curp.x, curp.y);
          case 'C':
            i = curve(shp, curp, relp, refp, strp, tags, i);
            break;

          case 's':
            relp.setLocation(curp.x, curp.y);
          case 'S':
            i = smooth(shp, curp, relp, refp, strp, tags, i);
            break;

          case 'l':
            relp.setLocation(curp.x, curp.y);
          case 'L':
            i = line(shp, curp, relp, refp, strp, tags, i);
            break;

          case 'h':
            relp.setLocation(curp.x, curp.y);
          case 'H':
            i = horizontal(shp, curp, relp, refp, strp, tags, i);
            break;

          case 'v':
            relp.setLocation(curp.x, curp.y);
          case 'V':
            i = vertical(shp, curp, relp, refp, strp, tags, i);
            break;
          }
      }
    return shp;
  }

  private int move(RShape shp, RPoint curp, RPoint relp, RPoint refp, RPoint strp, String[] tags, int i){
    shp.addMoveTo(PApplet.parseFloat(tags[i])+relp.x, PApplet.parseFloat(tags[i+1])+relp.y);

    curp.setLocation(PApplet.parseFloat(tags[i])+relp.x, PApplet.parseFloat(tags[i+1])+relp.y);
    refp.setLocation(curp.x,curp.y);
    strp.setLocation(curp.x,curp.y);
    //relp.setLocation(0F, 0F);
    return i + 1;
  }

  private int curve(RShape shp, RPoint curp, RPoint relp, RPoint refp, RPoint strp, String[] tags, int i){
    shp.addBezierTo(PApplet.parseFloat(tags[i])+relp.x, PApplet.parseFloat(tags[i+1])+relp.y, PApplet.parseFloat(tags[i+2])+relp.x, PApplet.parseFloat(tags[i+3])+relp.y, PApplet.parseFloat(tags[i+4])+relp.x, PApplet.parseFloat(tags[i+5])+relp.y);

    curp.setLocation(PApplet.parseFloat(tags[i+4])+relp.x, PApplet.parseFloat(tags[i+5])+relp.y);
    refp.setLocation(2.0f*curp.x-(PApplet.parseFloat(tags[i+2])+relp.x), 2.0f*curp.y-(PApplet.parseFloat(tags[i+3])+relp.y));
    return i + 5;
  }

  private int smooth(RShape shp, RPoint curp, RPoint relp, RPoint refp, RPoint strp, String[] tags, int i){
    shp.addBezierTo(refp.x, refp.y, PApplet.parseFloat(tags[i])+relp.x, PApplet.parseFloat(tags[i+1])+relp.y, PApplet.parseFloat(tags[i+2])+relp.x, PApplet.parseFloat(tags[i+3])+relp.y);

    curp.setLocation(PApplet.parseFloat(tags[i+2])+relp.x, PApplet.parseFloat(tags[i+3])+relp.y);
    refp.setLocation(2.0f*curp.x-(PApplet.parseFloat(tags[i])+relp.x), 2.0f*curp.y-(PApplet.parseFloat(tags[i+1])+relp.y));
    return i + 3;
  }

  private int line(RShape shp, RPoint curp, RPoint relp, RPoint refp, RPoint strp, String[] tags, int i){
    shp.addLineTo(PApplet.parseFloat(tags[i])+relp.x, PApplet.parseFloat(tags[i+1])+relp.y);

    curp.setLocation(PApplet.parseFloat(tags[i])+relp.x, PApplet.parseFloat(tags[i+1])+relp.y);
    refp.setLocation(curp.x, curp.y);
    return i + 1;
  }

  private int horizontal(RShape shp, RPoint curp, RPoint relp, RPoint refp, RPoint strp, String[] tags, int i){
    shp.addLineTo(PApplet.parseFloat(tags[i])+relp.x, curp.y);

    curp.setLocation(PApplet.parseFloat(tags[i])+relp.x, curp.y);
    refp.setLocation(curp.x, curp.y);
    return i;
  }

  private int vertical(RShape shp, RPoint curp, RPoint relp, RPoint refp, RPoint strp, String[] tags, int i){
    shp.addLineTo(curp.x, PApplet.parseFloat(tags[i])+relp.y);

    curp.setLocation(curp.x, PApplet.parseFloat(tags[i])+relp.y);
    refp.setLocation(curp.x, curp.y);
    return i;
  }
}
