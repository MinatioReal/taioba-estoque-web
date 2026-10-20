'use strict';
const $ = s => document.querySelector(s);
const state = { products: [], suppliers: [], movements: [], category: 'Todas', session: null, editor: null };
const money = n => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(n);
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const isAdmin = () => state.session?.perfil === 'ADMIN';
const canMove = () => ['ADMIN', 'OPERADOR'].includes(state.session?.perfil);
const normalize = s => s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
let toastTimer;
function toast(message) { $('#toast').textContent = message; $('#toast').hidden = false; clearTimeout(toastTimer); toastTimer = setTimeout(() => $('#toast').hidden = true, 5000); }
async function api(url, options = {}) {
  const response = await fetch(url, { ...options, headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': state.session?.csrf || '', ...options.headers } });
  if (response.status === 401) { await session(); throw new Error('Sua sessão terminou. Entre novamente.'); }
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.mensagem || (response.status === 403 ? 'Você não tem permissão para essa ação. Atualize a página se sua sessão mudou.' : 'Não foi possível concluir. Tente novamente.'));
  }
  return response.status === 204 ? null : response.text().then(t => t ? JSON.parse(t) : null);
}
async function session() {
  state.session = await fetch('/api/sessao').then(r => { if (!r.ok) throw new Error('Servidor indisponível.'); return r.json(); });
  $('#login-screen').hidden = state.session.autenticado;
  $('#app').hidden = !state.session.autenticado;
  if (state.session.autenticado) {
    $('#user-name').textContent = `${state.session.usuario} · ${ {ADMIN:'Administrador',OPERADOR:'Operador',AUDITOR:'Auditor'}[state.session.perfil] || state.session.perfil }`;
    $('#avatar').textContent = state.session.usuario[0].toUpperCase();
    document.querySelectorAll('.admin-only').forEach(el => el.hidden = !isAdmin());
    document.querySelectorAll('.move-only').forEach(el => el.hidden = !canMove());
    $('#hero-action').textContent = canMove() ? 'Registrar movimentação ↗' : 'Ver movimentações ↗';
  }
}
async function refresh() {
  const [products, suppliers, movements] = await Promise.all([api('/api/produtos'), api('/api/fornecedores'), api('/api/movimentacoes')]);
  Object.assign(state, { products, suppliers, movements });
  if (!['Todas', ...products.map(p => p.categoria)].includes(state.category)) state.category = 'Todas';
  const supplier = $('#supplier-filter').value;
  $('#supplier-filter').innerHTML = '<option value="">Todos os fornecedores</option>' + suppliers.map(f => `<option value="${f.id}">${esc(f.nome)}</option>`).join('');
  $('#supplier-filter').value = suppliers.some(f => String(f.id) === supplier) ? supplier : '';
  $('#stat-products').textContent = products.length;
  $('#stat-items').textContent = products.reduce((n, p) => n + p.quantidade, 0).toLocaleString('pt-BR');
  // Soma centavos inteiros; a regra financeira oficial fica no domínio Java com BigDecimal.
  $('#stat-value').textContent = money(products.reduce((n, p) => n + Math.round(p.preco * 100) * p.quantidade, 0) / 100);
  $('#stat-alerts').textContent = products.filter(p => p.quantidade <= p.minimo).length;
  renderProducts(); renderSuppliers(); renderMovements(); route();
}
function art(p) {
  const name = normalize(p.nome);
  let shape;
  if (name.includes('mousepad')) shape = '<path d="M15 43 132 32 153 96 28 110Z" fill="#375346"/><path d="m23 48 103-9 23 50-116 12Z" fill="#293c34"/><path d="m34 57 81-7" stroke="#98bb79" stroke-width="2"/>';
  else if (name.includes('mouse')) shape = '<path d="M70 16c-23 0-32 23-32 48 0 33 12 47 33 47s32-16 32-47c0-26-10-48-33-48Z" fill="#30443c"/><path d="M70 18v43m-31 2h64" stroke="#90ad93" stroke-width="2"/><rect x="66" y="30" width="8" height="18" rx="4" fill="#b9df89"/><path d="M43 79c2 18 10 25 26 27" fill="none" stroke="#a2c674" stroke-width="2"/>';
  else if (name.includes('teclado')) shape = '<path d="m9 45 130-9 19 56-139 9Z" fill="#283c34"/><path d="m17 49 116-6 14 40-124 8Z" fill="#4e6558"/>' + Array.from({length:36},(_,i)=>`<rect x="${22+i%12*10}" y="${51+Math.floor(i/12)*12}" width="7" height="8" rx="1" fill="${i%5===0?'#adc98f':'#cbd2c4'}"/>`).join('') + '<path d="M55 91h57" stroke="#c5d5bb" stroke-width="5"/>';
  else if (name.includes('monitor')) shape = '<rect x="12" y="14" width="142" height="87" rx="5" fill="#2d4038"/><rect x="18" y="20" width="130" height="72" rx="2" fill="#a7bd9e"/><path d="m18 79 45-45 32 31 21-16 32 30v13H18Z" fill="#70916d"/><circle cx="119" cy="37" r="9" fill="#d9e6c5"/><path d="M77 101v16m-21 3h54" stroke="#425a4c" stroke-width="7"/>';
  else if (name.includes('headset')) shape = '<path d="M39 72V55a44 44 0 0 1 88 0v19" fill="none" stroke="#30443b" stroke-width="12"/><path d="M43 51a40 40 0 0 1 80 0" fill="none" stroke="#7b9279" stroke-width="4"/><rect x="28" y="58" width="27" height="44" rx="12" fill="#3c5146"/><rect x="111" y="58" width="27" height="44" rx="12" fill="#3c5146"/><path d="m128 94-16 18H91" fill="none" stroke="#30443b" stroke-width="5"/><rect x="80" y="107" width="20" height="9" rx="4" fill="#829a71"/>';
  else if (name.includes('webcam')) shape = '<rect x="28" y="35" width="110" height="57" rx="23" fill="#33473c"/><circle cx="83" cy="63" r="22" fill="#1b3026"/><circle cx="83" cy="63" r="13" fill="#718d7f"/><circle cx="79" cy="59" r="5" fill="#abc8b8"/><circle cx="123" cy="57" r="3" fill="#b5d38b"/><path d="M70 94v15h29V94" fill="#596c5a"/>';
  else if (name.includes('ram')) shape = '<path d="m15 52 132-13 7 52L22 105Z" fill="#37634b"/><path d="m24 98 127-13" stroke="#c8b77c" stroke-width="7"/>' + [0,1,2,3].map(i=>`<rect x="${27+i*29}" y="${61-i*3}" width="22" height="26" rx="2" fill="#263b31"/>`).join('');
  else shape = '<rect x="30" y="19" width="103" height="94" rx="8" fill="#344b3d"/><rect x="42" y="33" width="79" height="57" rx="3" fill="#d0dabc"/><path d="M55 46h51M55 55h32" stroke="#8ba078" stroke-width="3"/><text x="82" y="79" text-anchor="middle" font-family="Arial" font-size="17" font-weight="bold" fill="#34563e">TAIOBA</text><path d="M52 105h60" stroke="#b9aa65" stroke-width="5"/>';
  return `<svg viewBox="0 0 166 130" aria-hidden="true">${shape}</svg>`;
}
function renderProducts() {
  const categories = ['Todas', ...new Set(state.products.map(p => p.categoria).sort())];
  $('#categories').innerHTML = categories.map(c => `<button class="${c === state.category ? 'active' : ''}" aria-pressed="${c === state.category}" data-category="${esc(c)}">${esc(c)}</button>`).join('');
  const query = normalize($('#search').value.trim());
  const status = $('#stock-filter').value, supplier = $('#supplier-filter').value;
  const products = state.products.filter(p => (state.category === 'Todas' || p.categoria === state.category)
    && normalize(`${p.nome} ${p.descricao} ${p.fornecedor}`).includes(query)
    && (!supplier || String(p.fornecedorId) === supplier)
    && (status === 'todos' || (status === 'zerado' ? p.quantidade === 0 : p.quantidade <= p.minimo)));
  const sorts = {nome:(a,b)=>a.nome.localeCompare(b.nome,'pt-BR'),preco:(a,b)=>a.preco-b.preco,quantidade:(a,b)=>a.quantidade-b.quantidade,recentes:(a,b)=>b.id-a.id};
  products.sort(sorts[$('#sort').value]);
  $('#product-count').textContent = state.products.length;
  $('#results-label').textContent = `${products.length} de ${state.products.length} produtos${query ? ` · Busca por “${$('#search').value.trim()}”` : ' · Um olhar sobre o seu estoque'}`;
  $('#product-grid').innerHTML = products.length ? products.map(p => {
    const badge = p.quantidade === 0 ? ['zero','Sem estoque'] : p.quantidade <= p.minimo ? ['low','Repor estoque'] : ['','Em estoque'];
    return `<article class="product-card"><div class="product-art" data-category="${esc(p.categoria)}"><span class="badge ${badge[0]}">${badge[1]}</span>${art(p)}</div><div class="product-info"><span class="product-category">${esc(p.categoria)}</span><h3>${esc(p.nome)}</h3><p class="product-description">${esc(p.descricao)}</p><div class="price">${money(p.preco)}</div><p class="product-supplier">por ${esc(p.fornecedor)}</p><div class="product-bottom"><span class="stock-count"><strong>${p.quantidade}</strong> un. disponíveis</span><div class="card-actions">${canMove()?`<button data-move="${p.id}" aria-label="Movimentar ${esc(p.nome)}">⇄</button>`:''}${isAdmin()?`<button data-edit="${p.id}" aria-label="Editar ${esc(p.nome)}">Editar</button>`:''}</div></div></div></article>`;
  }).join('') : '<div class="empty">Nenhum produto encontrado.<br><button class="button secondary" id="clear-filters">Limpar filtros</button></div>';
}
function renderSuppliers() {
  $('#supplier-grid').innerHTML = state.suppliers.length ? state.suppliers.map(f => `<article class="supplier-card"><span class="stat-icon">♧</span><h3>${esc(f.nome)}</h3><p>CNPJ: ${esc(f.cnpj)}<br>${esc(f.email)}<br>${esc(f.telefone)}</p>${isAdmin()?`<button class="button secondary" data-supplier-edit="${f.id}">Editar</button><button class="button secondary" data-supplier-delete="${f.id}">Excluir</button>`:''}</article>`).join('') : '<div class="empty">Nenhum fornecedor cadastrado.</div>';
}
function renderMovements() {
  $('#movement-rows').innerHTML = state.movements.length ? state.movements.map(m => `<tr><td>${esc(m.produto)}</td><td><span class="badge ${m.tipo==='SAIDA'?'low':''}">${m.tipo==='SAIDA'?'↗ Saída':'↙ Entrada'}</span></td><td>${m.quantidade} un.</td><td>${new Date(m.data).toLocaleString('pt-BR')}</td><td>${esc(m.usuario)}</td><td>${esc(m.observacao || '—')}</td></tr>`).join('') : '<tr><td colspan="6">Nenhuma movimentação registrada.</td></tr>';
}
function route() {
  const page = ['produtos','movimentacoes','fornecedores'].includes(location.hash.slice(1)) ? location.hash.slice(1) : 'produtos';
  $('#page-products').hidden = page !== 'produtos'; $('#page-movements').hidden = page !== 'movimentacoes'; $('#page-suppliers').hidden = page !== 'fornecedores';
  document.querySelectorAll('[data-page]').forEach(a => {a.classList.toggle('active',a.dataset.page===page); if(a.dataset.page===page) a.setAttribute('aria-current','page'); else a.removeAttribute('aria-current');});
}
function field(name,label,value='',extra='') { return `<label>${label}<input name="${name}" value="${esc(value)}" ${extra}></label>`; }
function openEditor(kind,id) {
  state.editor = { kind, id }; $('#editor-error').textContent = ''; $('#save-editor').textContent = 'Salvar';
  if(kind === 'product') {
    if(!state.suppliers.length) return toast('Cadastre um fornecedor antes de adicionar produtos.');
    const p = state.products.find(p => p.id === id) || {};
    $('#editor-title').textContent = id ? 'Editar produto' : 'Novo produto';
    $('#editor-fields').innerHTML = field('nome','Nome do produto',p.nome,'required maxlength="100"') + `<label>Descrição<textarea name="descricao" maxlength="500">${esc(p.descricao)}</textarea></label>`
      + field('categoria','Categoria',p.categoria,'required maxlength="50" list="category-options"') + `<datalist id="category-options">${[...new Set(state.products.map(p=>p.categoria))].map(c=>`<option value="${esc(c)}"></option>`).join('')}</datalist>`
      + '<div class="form-row">' + field('preco','Preço unitário (R$)',p.preco ?? '', 'type="number" min="0" max="9999999999.99" step="0.01" required') + field('minimo','Estoque mínimo',p.minimo ?? 5,'type="number" min="0" max="2147483647" step="1" required') + '</div>'
      + `<label>Fornecedor<select name="fornecedorId" required>${state.suppliers.map(f=>`<option value="${f.id}" ${f.id===p.fornecedorId?'selected':''}>${esc(f.nome)}</option>`).join('')}</select></label><p class="field-note">O saldo é atualizado por entradas e saídas, preservando o histórico.${id?` Saldo atual: ${p.quantidade} unidades.`:' Novos produtos começam com saldo zero.'}</p>`
      + (id?'<button class="button secondary" type="button" id="delete-product">Excluir produto</button>':'');
  } else if(kind === 'movement') {
    if(!state.products.length) return toast('Cadastre um produto primeiro.');
    $('#editor-title').textContent = 'Registrar movimentação';
    $('#editor-fields').innerHTML = `<label>Produto<select name="produtoId" required>${state.products.map(p=>`<option value="${p.id}" ${p.id===id?'selected':''}>${esc(p.nome)} · ${p.quantidade} un.</option>`).join('')}</select></label><div class="form-row"><label>Tipo<select name="tipo"><option value="ENTRADA">Entrada</option><option value="SAIDA">Saída</option></select></label>${field('quantidade','Quantidade','','type="number" min="1" max="2147483647" step="1" required')}</div><label>Observação<textarea name="observacao" maxlength="250" placeholder="Ex.: Reposição do fornecedor"></textarea></label><p class="field-note">O saldo e o histórico são atualizados juntos. Saídas acima do disponível serão recusadas.</p>`;
  } else {
    const f = state.suppliers.find(f=>f.id===id)||{};
    $('#editor-title').textContent = id ? 'Editar fornecedor' : 'Novo fornecedor';
    $('#editor-fields').innerHTML = field('nome','Nome do fornecedor',f.nome,'required maxlength="100"') + field('cnpj','CNPJ',f.cnpj,'required pattern="[0-9]{2}\\.[0-9]{3}\\.[0-9]{3}/[0-9]{4}-[0-9]{2}" placeholder="12.345.678/0001-90" title="Use o formato 12.345.678/0001-90"') + field('email','E-mail',f.email,'type="email" required maxlength="120"') + field('telefone','Telefone',f.telefone,'type="tel" required maxlength="25"');
  }
  $('#editor').showModal();
}
$('#editor-form').addEventListener('submit',async e => {
  e.preventDefault(); const button = $('#save-editor'); button.disabled = true; $('#editor-error').textContent = '';
  try {
    const data = Object.fromEntries(new FormData(e.target)), {kind,id} = state.editor;
    let url, method;
    if(kind === 'product') { ['preco','minimo','fornecedorId'].forEach(k=>data[k]=Number(data[k])); url='/api/produtos'; }
    else if(kind === 'movement') { ['produtoId','quantidade'].forEach(k=>data[k]=Number(data[k])); url='/api/movimentacoes'; }
    else url='/api/fornecedores';
    method = id && kind!=='movement' ? 'PUT' : 'POST';
    await api(url+(method==='PUT'?`/${id}`:''),{method,body:JSON.stringify(data)});
    $('#editor').close(); toast('Registro salvo com sucesso.');
    await refresh();
  } catch(error) { if($('#editor').open) $('#editor-error').textContent=error.message; else toast(error.message); }
  finally { button.disabled=false; }
});
$('#login-form').addEventListener('submit',async e => {
  e.preventDefault(); const button=e.target.querySelector('button'); button.disabled=true; $('#login-error').textContent='';
  try {
    await session();
    const body = new URLSearchParams(new FormData(e.target)); body.set('_csrf',state.session.csrf);
    const response=await fetch('/login',{method:'POST',body});
    if(!response.ok) throw new Error('Usuário ou senha incorretos.');
    e.target.reset(); await session(); await refresh();
  } catch(error) { $('#login-error').textContent=error.message; }
  finally { button.disabled=false; }
});
$('#logout').onclick = async () => { try { await api('/logout',{method:'POST'}); $('#editor').close(); await session(); } catch(e){toast(e.message);} };
$('#close-editor').onclick = $('#cancel-editor').onclick = () => $('#editor').close();
$('#new-product').onclick = () => openEditor('product');
$('#new-supplier').onclick = () => openEditor('supplier');
$('#new-movement').onclick = () => openEditor('movement');
$('#hero-action').onclick = () => { if(canMove()) openEditor('movement'); else location.hash='movimentacoes'; };
$('#search-form').onsubmit = e => { e.preventDefault(); location.hash='produtos'; renderProducts(); };
$('#search').oninput = () => { if(state.session?.autenticado) { location.hash='produtos'; renderProducts(); } };
['#stock-filter','#supplier-filter','#sort'].forEach(s=>$(s).onchange=renderProducts);
document.addEventListener('click',async e => {
  const b=e.target.closest('button'); if(!b) return;
  if(b.dataset.category) { state.category=b.dataset.category; renderProducts(); }
  if(b.dataset.edit) openEditor('product',Number(b.dataset.edit));
  if(b.dataset.move) openEditor('movement',Number(b.dataset.move));
  if(b.dataset.supplierEdit) openEditor('supplier',Number(b.dataset.supplierEdit));
  if(b.id==='clear-filters') { $('#search').value='';$('#stock-filter').value='todos';$('#supplier-filter').value='';state.category='Todas';renderProducts(); }
  if(b.dataset.supplierDelete || b.id==='delete-product') {
    const supplier=!!b.dataset.supplierDelete, id=supplier?Number(b.dataset.supplierDelete):state.editor.id;
    if(!confirm('Excluir este registro? Essa ação não pode ser desfeita. Registros com vínculos ou histórico serão preservados.')) return;
    b.disabled=true;
    try { await api(`/api/${supplier?'fornecedores':'produtos'}/${id}`,{method:'DELETE'});$('#editor').close();toast('Registro excluído.');await refresh(); }
    catch(error) { if($('#editor').open) $('#editor-error').textContent=error.message; else toast(error.message); }
    finally { b.disabled=false; }
  }
});
window.addEventListener('hashchange',route);
(async()=>{try{await session();if(state.session.autenticado)await refresh();}catch(e){$('#login-screen').hidden=false;$('#login-error').textContent='Não foi possível acessar o servidor local. '+e.message;}})();
