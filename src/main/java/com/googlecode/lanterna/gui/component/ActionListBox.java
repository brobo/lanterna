/*
 * This file is part of lanterna (http://code.google.com/p/lanterna/).
 * 
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2010-2012 Martin
 */

package com.googlecode.lanterna.gui.component;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Interactable;
import com.googlecode.lanterna.gui.TextGraphics;
import com.googlecode.lanterna.gui.Theme.Category;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalPosition;
import com.googlecode.lanterna.terminal.TerminalSize;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin
 */
public class ActionListBox extends AbstractInteractableComponent
{
    private final List<Item> itemList;
    private final int forceWidth;
    private int selectedIndex;

    public ActionListBox()
    {
        this(-1);
    }

    public ActionListBox(int forceWidth)
    {
        this.itemList = new ArrayList<Item>();
        this.forceWidth = forceWidth;
        this.selectedIndex = -1;
    }

    public void addItem(final Item item)
    {
        itemList.add(item);
        if(selectedIndex == -1)
            selectedIndex = 0;
    }

    public void addAction(final Action action)
    {
        addItem(new Item() {
            public String getTitle() {
                return action.toString();
            }

            public void doAction() {
                action.doAction();
            }
        });
    }

    public void clearItems()
    {
        itemList.clear();
        selectedIndex = -1;
    }

    public int getSelectedItemIndex()
    {
        return selectedIndex;
    }

    public Item getItem(int index)
    {
        return itemList.get(index);
    }

    public int getNrOfItems()
    {
        return itemList.size();
    }

    public void setSelectedIndex(int index)
    {
        if(index < -1)
            index = -1;

        if(index == -1 && getNrOfItems() > 0)
            selectedIndex = 0;
        else if(index != -1 && index >= getNrOfItems())
            selectedIndex = getNrOfItems() - 1;
        else
            selectedIndex = index;
        invalidate();
    }

    public void repaint(TextGraphics graphics)
    {
        for(int i = 0; i < itemList.size(); i++) {
            if(selectedIndex == i && hasFocus())
                graphics.applyTheme(Category.ListItemSelected);
            else
                graphics.applyTheme(Category.ListItem);

            String title = itemList.get(i).getTitle();
            if(title.length() > graphics.getWidth() && graphics.getWidth() > 3)
                title = title.substring(0, graphics.getWidth() - 3) + "...";

            graphics.drawString(0, i, title);
        }
        if(selectedIndex == -1)
            setHotspot(new TerminalPosition(0, 0));
        else
            setHotspot(graphics.translateToGlobalCoordinates(new TerminalPosition(0, selectedIndex)));
    }

    public TerminalSize getPreferredSize()
    {
        if(itemList.isEmpty())
            return new TerminalSize(1,1);
        
        if(forceWidth != -1)
            return new TerminalSize(forceWidth, itemList.size());

        int maxLength = 0;
        for(Item item: itemList) {
            if(item.getTitle().length() > maxLength)
                maxLength = item.getTitle().length();
        }
        return new TerminalSize(maxLength, itemList.size());
    }
    
    @Override
    protected void afterEnteredFocus(FocusChangeDirection direction)
    {
        if(direction == FocusChangeDirection.DOWN)
            selectedIndex = 0;
        else if(direction == FocusChangeDirection.UP)
            selectedIndex = itemList.size() - 1;
    }

    public Interactable.Result keyboardInteraction(Key key)
    {
        try {
            switch(key.getKind()) {
                case Tab:
                case ArrowRight:
                    return Result.NEXT_INTERACTABLE_RIGHT;

                case ReverseTab:
                case ArrowLeft:
                    return Result.PREVIOUS_INTERACTABLE_LEFT;

                case ArrowDown:
                    if(selectedIndex == itemList.size() - 1)
                        return Result.NEXT_INTERACTABLE_DOWN;
                    else
                        selectedIndex++;
                    break;

                case ArrowUp:
                    if(selectedIndex == 0)
                        return Result.PREVIOUS_INTERACTABLE_UP;
                    else
                        selectedIndex--;
                    break;

                case Enter:
                    if(selectedIndex != -1)
                        itemList.get(selectedIndex).doAction();
                    break;

                case PageDown:
                    selectedIndex = itemList.size() - 1;
                    break;

                case PageUp:
                    selectedIndex = 0;
                    break;
            }
            return Result.DO_NOTHING;
        }
        finally {
            invalidate();
        }
    }

    public static interface Item
    {
        public String getTitle();
        public void doAction();
    }
}