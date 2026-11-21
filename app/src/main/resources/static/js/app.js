/* Small progressive-enhancement layer. The pages work without it; it adds local time, number formatting and filters. */
(function () {
  'use strict';
  var lang = document.documentElement.lang || undefined;
  var tz = '';
  try { tz = Intl.DateTimeFormat().resolvedOptions().timeZone || ''; } catch (e) { /* old browser */ }

  // Times arrive as UTC (shown as GMT without JavaScript). Show them in the visitor's own time zone.
  function localTimes(root) {
    root.querySelectorAll('time[datetime]').forEach(function (el) {
      var d = new Date(el.getAttribute('datetime'));
      if (isNaN(d)) return;
      if (!el.title) el.title = el.textContent.trim();
      try {
        el.textContent = new Intl.DateTimeFormat(lang, { dateStyle: 'medium', timeStyle: 'short', timeZoneName: 'short' }).format(d);
      } catch (e) { /* keep the GMT text */ }
    });
  }

  // Numbers keep their decimals but get thousands separators in the visitor's locale.
  function numbers(root) {
    root.querySelectorAll('[data-num]').forEach(function (el) {
      var raw = (el.getAttribute('data-raw') || el.textContent).trim();
      var n = Number(raw);
      if (raw === '' || !isFinite(n)) return;
      el.setAttribute('data-raw', raw);
      var decimals = (raw.split('.')[1] || '').length;
      el.textContent = new Intl.NumberFormat(lang, { minimumFractionDigits: decimals, maximumFractionDigits: decimals }).format(n);
    });
  }

  function enhance(root) { localTimes(root); numbers(root); }

  // The PDF is rendered on the server, tell it the visitor's time zone.
  document.querySelectorAll('a[data-pdf]').forEach(function (a) {
    if (tz) a.href = a.getAttribute('href') + (a.href.indexOf('?') < 0 ? '?' : '&') + 'tz=' + encodeURIComponent(tz);
  });
  enhance(document);
  document.body.addEventListener('htmx:afterSwap', function (e) { enhance(e.target); });

  // Mobile menu
  var menu = document.getElementById('menu-btn');
  if (menu) menu.addEventListener('click', function () { document.body.classList.toggle('nav-open'); });
  document.addEventListener('click', function (e) {
    if (document.body.classList.contains('nav-open') && !e.target.closest('.sidebar') && !e.target.closest('#menu-btn') && !e.target.closest('.tour-card')) {
      document.body.classList.remove('nav-open');
    }
  });

  // Toast
  var toast = document.getElementById('toast');
  if (toast) setTimeout(function () { toast.hidden = true; }, 5000);

  // Confirm dangerous forms, block double submit
  document.querySelectorAll('form[data-confirm]').forEach(function (f) {
    f.addEventListener('submit', function (e) { if (!window.confirm(f.getAttribute('data-confirm'))) e.preventDefault(); });
  });
  document.querySelectorAll('form').forEach(function (f) {
    f.addEventListener('submit', function (e) {
      if (e.defaultPrevented) return;
      f.querySelectorAll('button[type=submit]').forEach(function (b) { setTimeout(function () { b.disabled = true; }, 0); });
    });
  });
  window.addEventListener('pageshow', function () {
    document.querySelectorAll('button[type=submit]').forEach(function (b) { b.disabled = false; });
  });

  // Calculation list: instant search and status filter
  var table = document.getElementById('calc-table');
  if (table) {
    var search = document.getElementById('calc-search');
    var segments = document.querySelectorAll('[data-filter]');
    var empty = document.getElementById('no-match');
    var state = 'all';
    var apply = function () {
      var q = (search ? search.value : '').trim().toLowerCase();
      var visible = 0;
      table.querySelectorAll('tbody tr[data-calc-title]').forEach(function (row) {
        var statusOk = state === 'all' ||
          (state === 'active' && row.dataset.archived !== 'true') ||
          (state === 'archived' && row.dataset.archived === 'true') ||
          (state === 'frozen' && row.dataset.frozen === 'true');
        var textOk = !q || row.textContent.toLowerCase().indexOf(q) >= 0;
        row.hidden = !(statusOk && textOk);
        if (!row.hidden) visible++;
      });
      if (empty) empty.hidden = visible > 0;
    };
    if (search) search.addEventListener('input', apply);
    segments.forEach(function (b) {
      b.addEventListener('click', function () {
        state = b.getAttribute('data-filter');
        segments.forEach(function (x) { x.classList.toggle('on', x === b); });
        apply();
      });
    });
  }

  // Every table row with data-href opens its page when clicked anywhere (links and buttons inside keep working)
  document.querySelectorAll('tr[data-href]').forEach(function (row) {
    row.addEventListener('click', function (e) {
      if (e.target.closest('a, button, form, input, select, summary')) return;
      if (window.getSelection && String(window.getSelection())) return;
      window.location.href = row.getAttribute('data-href');
    });
  });

  // Calculation form: show the recipe of the selected batch
  function showMix(value) {
    document.querySelectorAll('.mix-hint').forEach(function (p) { p.hidden = p.getAttribute('data-mix') !== value; });
    document.querySelectorAll('[data-mix-pick]').forEach(function (row) {
      row.classList.toggle('active', row.getAttribute('data-mix-pick') === value);
    });
  }
  document.querySelectorAll('input[name=batch]').forEach(function (radio) {
    radio.addEventListener('change', function () { if (radio.checked) showMix(radio.value); });
  });
  // Clicking a mix in the comparison selects it in the form (and refreshes the estimate)
  document.querySelectorAll('[data-mix-pick]').forEach(function (row) {
    row.addEventListener('click', function () {
      var radio = document.querySelector('input[name=batch][value="' + row.getAttribute('data-mix-pick') + '"]');
      if (!radio) return;
      radio.checked = true;
      radio.dispatchEvent(new Event('change', { bubbles: true }));
    });
  });

  // Demo accounts on the login page
  document.querySelectorAll('[data-fill-user]').forEach(function (b) {
    b.addEventListener('click', function () {
      document.getElementById('username').value = b.getAttribute('data-fill-user');
      document.getElementById('password').value = b.getAttribute('data-fill-password');
      document.getElementById('password').focus();
    });
  });

  // Projects list: instant search and currency filter
  var ptable = document.getElementById('project-table');
  if (ptable) {
    var psearch = document.getElementById('project-search');
    var pcur = document.getElementById('project-currency');
    var pempty = document.getElementById('no-project-match');
    var papply = function () {
      var q = (psearch.value || '').trim().toLowerCase();
      var cur = pcur ? pcur.value : '';
      var visible = 0;
      ptable.querySelectorAll('tbody tr[data-project]').forEach(function (row) {
        var curCell = row.querySelector('[data-currency]');
        var ok = (!cur || (curCell && curCell.dataset.currency === cur)) && (!q || row.textContent.toLowerCase().indexOf(q) >= 0);
        row.hidden = !ok;
        if (ok) visible++;
      });
      if (pempty) pempty.hidden = visible > 0;
    };
    psearch.addEventListener('input', papply);
    if (pcur) pcur.addEventListener('change', papply);
  }

  // Worker picker: search on the server (there can be thousands of workers), keep the choice as hidden inputs
  document.querySelectorAll('[data-picker]').forEach(function (root) {
    var url = root.getAttribute('data-url');
    var field = root.getAttribute('data-field');
    var multi = root.getAttribute('data-mode') === 'multi';
    var input = root.querySelector('.picker-input');
    var results = root.querySelector('.picker-results');
    var selected = root.querySelector('.picker-selected');
    var counter = root.querySelector('[data-count]');
    var timer = null;
    var seq = 0;

    function chosen() {
      return Array.prototype.map.call(selected.querySelectorAll('input[type=hidden]'), function (i) { return i.value; });
    }
    function refreshCount() { if (counter) counter.textContent = chosen().length; }
    function hideChosen() {
      var ids = chosen();
      results.querySelectorAll('[data-pick]').forEach(function (b) { b.hidden = ids.indexOf(b.getAttribute('data-id')) >= 0; });
    }
    function add(id, name) {
      if (!multi) selected.innerHTML = '';
      if (chosen().indexOf(id) >= 0) return;
      var chip = document.createElement('span');
      chip.className = 'chip-person';
      chip.setAttribute('data-id', id);
      var av = document.createElement('span');
      av.className = 'avatar sm';
      av.textContent = name.charAt(0);
      var label = document.createElement('span');
      label.textContent = name;
      var remove = document.createElement('button');
      remove.type = 'button';
      remove.className = 'btn-icon';
      remove.setAttribute('data-remove', '');
      remove.setAttribute('aria-label', 'Remove');
      remove.textContent = '×';
      var hidden = document.createElement('input');
      hidden.type = 'hidden';
      hidden.name = field;
      hidden.value = id;
      [av, label, remove, hidden].forEach(function (n) { chip.appendChild(n); });
      selected.appendChild(chip);
      refreshCount();
    }
    function search() {
      var mine = ++seq;
      fetch(url + '?q=' + encodeURIComponent(input.value.trim()), { credentials: 'same-origin' })
        .then(function (r) { return r.text(); })
        .then(function (html) {
          if (mine !== seq) return;
          results.innerHTML = html;
          results.hidden = false;
          hideChosen();
        });
    }
    input.addEventListener('input', function () { clearTimeout(timer); timer = setTimeout(search, 200); });
    input.addEventListener('focus', search);
    input.addEventListener('keydown', function (e) { if (e.key === 'Enter') e.preventDefault(); });
    results.addEventListener('click', function (e) {
      var b = e.target.closest('[data-pick]');
      if (!b) return;
      add(b.getAttribute('data-id'), b.getAttribute('data-name'));
      if (multi) { b.hidden = true; input.focus(); } else { results.hidden = true; input.value = ''; }
    });
    selected.addEventListener('click', function (e) {
      var r = e.target.closest('[data-remove]');
      if (r) { r.closest('.chip-person').remove(); refreshCount(); hideChosen(); }
    });
    document.addEventListener('click', function (e) { if (!root.contains(e.target)) results.hidden = true; });
    var form = root.closest('form');
    if (form && root.hasAttribute('data-required')) {
      form.addEventListener('submit', function (e) {
        if (chosen().length === 0) { e.preventDefault(); input.focus(); search(); }
      });
    }
    refreshCount();
  });

  // Print: load the PDF into a hidden frame and open the print dialog right away
  document.querySelectorAll('[data-print]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var url = btn.getAttribute('data-url') + (tz ? '&tz=' + encodeURIComponent(tz) : '');
      btn.disabled = true;
      fetch(url, { credentials: 'same-origin' })
        .then(function (r) { if (!r.ok) throw new Error('pdf'); return r.blob(); })
        .then(function (blob) {
          var objectUrl = URL.createObjectURL(blob);
          var frame = document.createElement('iframe');
          frame.style.cssText = 'position:fixed;right:0;bottom:0;width:0;height:0;border:0;visibility:hidden';
          frame.onload = function () {
            try { frame.contentWindow.focus(); frame.contentWindow.print(); }
            catch (e) { window.open(objectUrl, '_blank'); }
            btn.disabled = false;
          };
          frame.src = objectUrl;
          document.body.appendChild(frame);
        })
        .catch(function () { btn.disabled = false; window.open(url, '_blank'); });
    });
  });
  // Tooltips: one floating element, so they are never clipped by scrolling tables or cards
  var tipEl = document.createElement('div');
  tipEl.className = 'tooltip';
  tipEl.id = 'tooltip';
  tipEl.setAttribute('role', 'tooltip');
  document.body.appendChild(tipEl);
  var tipTarget = null, tipTimer = null;
  function showTip(el) {
    var text = el.getAttribute('data-tip');
    if (!text) return;
    tipTarget = el;
    tipEl.textContent = text;
    tipEl.classList.remove('on');
    tipEl.style.left = '0px'; tipEl.style.top = '0px';
    var r = el.getBoundingClientRect(), t = tipEl.getBoundingClientRect();
    var left = Math.min(Math.max(8, r.left + r.width / 2 - t.width / 2), window.innerWidth - t.width - 8);
    var top = r.top - t.height - 8;
    if (top < 8) top = r.bottom + 8;
    tipEl.style.left = left + 'px';
    tipEl.style.top = top + 'px';
    tipEl.classList.add('on');
    el.setAttribute('aria-describedby', 'tooltip');
  }
  function hideTip() {
    clearTimeout(tipTimer);
    tipEl.classList.remove('on');
    if (tipTarget) { tipTarget.classList.remove('open'); tipTarget.removeAttribute('aria-describedby'); }
    tipTarget = null;
  }
  document.addEventListener('mouseover', function (e) {
    var el = e.target.closest ? e.target.closest('[data-tip]') : null;
    if (!el || el === tipTarget) return;
    hideTip();
    tipTimer = setTimeout(function () { showTip(el); }, el.classList.contains('tip') ? 80 : 450);
  });
  document.addEventListener('mouseout', function (e) {
    var el = e.target.closest ? e.target.closest('[data-tip]') : null;
    if (el && (!e.relatedTarget || !el.contains(e.relatedTarget))) hideTip();
  });
  document.addEventListener('focusin', function (e) {
    var el = e.target.closest ? e.target.closest('[data-tip]') : null;
    if (el) showTip(el);
  });
  document.addEventListener('focusout', hideTip);
  document.addEventListener('scroll', hideTip, true);
  // Touch screens have no hover: tap the "?" icon to open its text, tap anywhere else to close it
  document.addEventListener('click', function (e) {
    var icon = e.target.closest ? e.target.closest('.tip') : null;
    if (icon) {
      e.preventDefault();
      if (tipTarget === icon && tipEl.classList.contains('on') && icon.classList.contains('open')) { hideTip(); return; }
      showTip(icon);
      icon.classList.add('open');
    } else if (tipTarget && tipTarget.classList.contains('open')) {
      hideTip();
    }
  });

  // Access level menus: only one open at a time, close on outside click or Escape, picking the current level just closes
  document.addEventListener('click', function (e) {
    var current = e.target.closest ? e.target.closest('.menu-item[data-current="true"]') : null;
    if (current) { e.preventDefault(); current.closest('details').open = false; return; }
    document.querySelectorAll('details.menu[open]').forEach(function (d) { if (!d.contains(e.target)) d.open = false; });
  });
  document.addEventListener('keydown', function (e) {
    if (e.key !== 'Escape') return;
    document.querySelectorAll('details.menu[open]').forEach(function (d) { d.open = false; var s = d.querySelector('summary'); if (s) s.focus(); });
  });

  // Welcome tour
  var cfg = window.BLOCKCALC_TOUR;
  if (cfg) {
    var tourOpen = false;
    var fmt = function (text, a, b) { return String(text).replace('{0}', a).replace('{1}', b); };
    var markDone = function () {
      var form = document.getElementById('tour-done-form');
      if (!form) return;
      try {
        fetch(form.action, { method: 'POST', credentials: 'same-origin', body: new URLSearchParams(new FormData(form)) });
      } catch (e) { /* the tour simply opens again next time */ }
    };
    var runTour = function (firstTime) {
      if (tourOpen) return;
      var steps = cfg.steps.filter(function (st) {
        if (st.only === 'worker' && cfg.staff) return false;
        if (st.only === 'staff' && !cfg.staff) return false;
        return !st.target || document.querySelector('[data-tour="' + st.target + '"]');
      });
      if (!steps.length) return;
      tourOpen = true;
      var i = 0;
      var shade = document.createElement('div'); shade.className = 'tour-shade';
      var spot = document.createElement('div'); spot.className = 'tour-spot none';
      var card = document.createElement('div'); card.className = 'tour-card'; card.setAttribute('role', 'dialog'); card.setAttribute('aria-modal', 'true');
      document.body.appendChild(shade); document.body.appendChild(spot); document.body.appendChild(card);
      hideTip();

      var finish = function () {
        document.body.classList.remove('nav-open');
        [shade, spot, card].forEach(function (n) { n.remove(); });
        window.removeEventListener('resize', place);
        document.removeEventListener('keydown', onKey, true);
        tourOpen = false;
        if (firstTime) markDone();
      };
      var button = function (cls, text, fn) {
        var b = document.createElement('button');
        b.type = 'button'; b.className = 'btn ' + cls; b.textContent = text;
        b.addEventListener('click', fn);
        return b;
      };
      var place = function () {
        var st = steps[i];
        var el = st.target ? document.querySelector('[data-tour="' + st.target + '"]') : null;
        if (!el) {
          spot.className = 'tour-spot none'; spot.removeAttribute('style');
          card.className = 'tour-card center'; card.style.left = ''; card.style.top = '';
          return;
        }
        var r = el.getBoundingClientRect();
        var pad = 6;
        spot.className = 'tour-spot';
        spot.style.left = (r.left - pad) + 'px'; spot.style.top = (r.top - pad) + 'px';
        spot.style.width = (r.width + pad * 2) + 'px'; spot.style.height = (r.height + pad * 2) + 'px';
        card.className = 'tour-card';
        var cw = card.offsetWidth, ch = card.offsetHeight, vw = window.innerWidth, vh = window.innerHeight;
        var left, top;
        if (r.right + 16 + cw <= vw) { left = r.right + 16; top = r.top; }
        else if (r.left - 16 - cw >= 0) { left = r.left - 16 - cw; top = r.top; }
        else { left = r.left; top = r.bottom + 16; if (top + ch > vh - 8) top = r.top - ch - 16; }
        card.style.left = Math.min(Math.max(12, left), Math.max(12, vw - cw - 12)) + 'px';
        card.style.top = Math.min(Math.max(12, top), Math.max(12, vh - ch - 12)) + 'px';
      };
      var render = function () {
        var st = steps[i];
        var el = st.target ? document.querySelector('[data-tour="' + st.target + '"]') : null;
        // the sidebar is a slide-in menu on small screens
        document.body.classList.toggle('nav-open', !!(el && el.closest('.sidebar') && window.innerWidth <= 860));
        if (el && !el.closest('.sidebar')) el.scrollIntoView({ block: 'center', behavior: 'auto' });
        card.innerHTML = '';
        var dots = document.createElement('div'); dots.className = 'tour-dots';
        steps.forEach(function (_, n) { var d = document.createElement('i'); if (n === i) d.className = 'on'; dots.appendChild(d); });
        var h = document.createElement('h2'); h.textContent = st.title;
        var p = document.createElement('p'); p.textContent = st.text;
        var x = document.createElement('button'); x.type = 'button'; x.className = 'btn-icon tour-x'; x.setAttribute('aria-label', cfg.i18n.close); x.textContent = '×';
        x.addEventListener('click', finish);
        var foot = document.createElement('div'); foot.className = 'tour-foot';
        var count = document.createElement('span'); count.className = 'tour-step'; count.textContent = fmt(cfg.i18n.step, i + 1, steps.length);
        foot.appendChild(count);
        if (i > 0) foot.appendChild(button('ghost sm', cfg.i18n.back, function () { i--; render(); }));
        else foot.appendChild(button('ghost sm', cfg.i18n.skip, finish));
        var last = i === steps.length - 1;
        var next = button('primary sm', last ? cfg.i18n.done : cfg.i18n.next, function () { if (last) finish(); else { i++; render(); } });
        foot.appendChild(next);
        [x, dots, h, p, foot].forEach(function (n) { card.appendChild(n); });
        place();
        next.focus();
      };
      var onKey = function (e) {
        if (e.key === 'Escape') { e.preventDefault(); finish(); }
        else if (e.key === 'ArrowRight' && i < steps.length - 1) { i++; render(); }
        else if (e.key === 'ArrowLeft' && i > 0) { i--; render(); }
      };
      window.addEventListener('resize', place);
      document.addEventListener('keydown', onKey, true);
      render();
    };
    document.querySelectorAll('[data-tour-start]').forEach(function (b) { b.addEventListener('click', function () { runTour(false); }); });
    if (cfg.auto) setTimeout(function () { runTour(true); }, 350);
  }
})();
