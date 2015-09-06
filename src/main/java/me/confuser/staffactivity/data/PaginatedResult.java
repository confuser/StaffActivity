package me.confuser.staffactivity.data;

/*
 * CommandBook
 * Copyright (C) 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

// Modified for StaffActivity by James Mortemore (confuser)

import lombok.Getter;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Commands that wish to display a paginated list of results can use this class to do
 * the actual pagination, giving a list of items, a page number, and basic formatting information.
 */
public abstract class PaginatedResult<T> {

  private final Message header;
  protected final Message subhead;
  @Getter
  private final List<? extends T> results;

  protected static final int PER_PAGE = 9;

  public PaginatedResult(Message header, Message subhead, List<? extends T> results) {
    this.header = header;
    this.subhead = subhead;
    this.results = results;
  }

  public void display(CommandSender sender, int page) {
    if (results.size() == 0) {
      String subHead = formatSubhead();
      if (subHead != null && !subHead.isEmpty()) sender.sendMessage(subHead);

      sender.sendMessage(noResults());
      return;
    }
    --page;

    int maxPages = results.size() / PER_PAGE;

    // If the content divides perfectly, eg (18 entries, and 9 per page)
    // we end up with a blank page this handles this case
    if (results.size() % PER_PAGE == 0) {
      maxPages--;
    }

    page = Math.max(0, Math.min(page, maxPages));

    formatHeader(header.set("page", page + 1).set("maxPage", maxPages + 1)).sendTo(sender);
    if (page == 0) {
      String subHead = formatSubhead();
      if (subHead != null && !subHead.isEmpty()) sender.sendMessage(subHead);
    }

    for (int i = PER_PAGE * page; i < PER_PAGE * page + PER_PAGE && i < results.size(); i++) {
      sender.sendMessage(format(results.get(i)));
    }
  }

  public boolean hasResults() {
    return results.size() != 0;
  }

  public abstract String format(T entry);

  public abstract String formatSubhead();

  protected abstract Message formatHeader(Message set);

  public abstract String noResults();

}

