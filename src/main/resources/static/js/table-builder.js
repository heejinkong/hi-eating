import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { RoomEnvironment } from 'three/addons/environments/RoomEnvironment.js';
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';

const page = document.querySelector('.table-builder-page');
const container = document.getElementById('table-viewer');
const statusEl = document.getElementById('table-builder-status');
const captureBtn = document.getElementById('table-capture-btn');
const resetBtn = document.getElementById('table-reset-btn');
const captureLink = document.getElementById('table-capture-link');
const searchInput = document.getElementById('table-product-search');
const productCards = Array.from(document.querySelectorAll('.table-product-card'));
const tableModelButtons = Array.from(document.querySelectorAll('.table-model-picker__button'));

const scene = new THREE.Scene();
scene.background = new THREE.Color(0xf8f4ef);

const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 1000);
camera.position.set(0, 4.5, 9);

const renderer = new THREE.WebGLRenderer({
  antialias: true,
  preserveDrawingBuffer: true,
});

renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
renderer.shadowMap.enabled = true;
renderer.shadowMap.type = THREE.PCFSoftShadowMap;
renderer.outputColorSpace = THREE.SRGBColorSpace;
renderer.toneMapping = THREE.ACESFilmicToneMapping;
renderer.toneMappingExposure = 1.45;
container.appendChild(renderer.domElement);

const pmremGenerator = new THREE.PMREMGenerator(renderer);
const environment = pmremGenerator.fromScene(new RoomEnvironment(), 0.04).texture;
scene.environment = environment;
pmremGenerator.dispose();

const controls = new OrbitControls(camera, renderer.domElement);
controls.target.set(0, 1.2, 0);
controls.enableDamping = true;
controls.dampingFactor = 0.05;
controls.minDistance = 3;
controls.maxDistance = 18;
controls.update();

const loader = new GLTFLoader();
loader.setCrossOrigin('anonymous');

const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();
const draggableObjects = [];
const foodModels = [];
const initialPositions = new Map();
const addedModelsByProductId = new Map();

let table = null;
let tableTopY = 0;
let selectedModel = null;
let lastSelectedModel = null;
let isDragging = false;
let isCapturing = false;
let isChangingTable = false;
let nextSlotIndex = 0;

const dragPlane = new THREE.Plane();
const dragPoint = new THREE.Vector3();
const dragOffset = new THREE.Vector3();

addLights();
addFloor();
resizeRenderer();
init();
animate();

function setStatus(message) {
  statusEl.textContent = message;
}

async function init() {
  try {
    const tableModelSrc = page.dataset.tableModelSrc || '/models/table.glb';
    table = await createTableModel(tableModelSrc);
    scene.add(table);
    updateTableTopY();
    setActiveTableButton(tableModelSrc);
    setStatus('식탁이 준비되었습니다.');
  } catch (error) {
    console.error(error);
    setStatus('식탁 모델을 불러오지 못했습니다.');
  }
}

async function createTableModel(url) {
  const tableModel = await loadGLB(url, { brighten: false });
  tableModel.name = 'table';
  tableModel.position.set(0, 0, 0);
  normalizeModel(tableModel, 7);
  return tableModel;
}

async function changeTableModel(button) {
  if (isChangingTable || button.classList.contains('is-active')) {
    return;
  }

  const nextTableSrc = button.dataset.tableModelSrc;

  try {
    isChangingTable = true;
    setTableModelButtonsDisabled(true);
    setStatus('테이블을 바꾸는 중입니다.');

    const nextTable = await createTableModel(nextTableSrc);
    const previousTable = table;

    table = nextTable;
    scene.add(table);
    scene.remove(previousTable);
    disposeModel(previousTable);

    updateTableTopY();
    alignFoodsToTableTop();
    setActiveTableButton(nextTableSrc);
    setStatus('선택한 테이블로 변경되었습니다.');
  } catch (error) {
    console.error(error);
    setStatus('테이블 모델을 불러오지 못했습니다.');
  } finally {
    isChangingTable = false;
    setTableModelButtonsDisabled(false);
  }
}

function updateTableTopY() {
  tableTopY = getModelTopY(table);
}

function alignFoodsToTableTop() {
  foodModels.forEach((model) => {
    const previousY = model.position.y;
    placeOnTable(model, model.position.x, tableTopY, model.position.z);
    const yDelta = model.position.y - previousY;
    const initial = initialPositions.get(model.uuid);

    if (initial) {
      initial.position.y += yDelta;
    }
  });
}

function setActiveTableButton(tableModelSrc) {
  tableModelButtons.forEach((button) => {
    const isActive =
      normalizeUrlPath(button.dataset.tableModelSrc) === normalizeUrlPath(tableModelSrc);
    button.classList.toggle('is-active', isActive);
    button.setAttribute('aria-pressed', String(isActive));
  });
}

function setTableModelButtonsDisabled(disabled) {
  tableModelButtons.forEach((button) => {
    button.disabled = disabled;
  });
}

function normalizeUrlPath(url) {
  return new URL(url, window.location.origin).pathname;
}

function loadGLB(url, options = {}) {
  const { brighten = false } = options;

  return new Promise((resolve, reject) => {
    loader.load(
      url,
      (gltf) => {
        const model = gltf.scene;

        model.traverse((child) => {
          if (!child.isMesh) {
            return;
          }

          child.castShadow = true;
          child.receiveShadow = true;

          if (!child.material) {
            return;
          }

          child.material = child.material.clone();
          child.material.side = THREE.DoubleSide;

          if (child.material.map) {
            child.material.map.colorSpace = THREE.SRGBColorSpace;
          }

          child.material.envMapIntensity = brighten ? 1.55 : 0.95;

          if (brighten) {
            if ('metalness' in child.material) {
              child.material.metalness = 0.05;
            }

            if ('roughness' in child.material) {
              child.material.roughness = 0.55;
            }

            if ('emissive' in child.material) {
              child.material.emissive = new THREE.Color(0x222222);
              child.material.emissiveIntensity = 0.08;
            }
          }

          child.material.needsUpdate = true;
        });

        resolve(model);
      },
      undefined,
      reject,
    );
  });
}

async function toggleProductOnTable(card) {
  const productId = card.dataset.productId;

  if (addedModelsByProductId.has(productId)) {
    removeProductFromTable(card);
    return;
  }

  await addProductToTable(card);
}

async function addProductToTable(card) {
  const { productName, glbSrc, productId } = card.dataset;
  const toggleButton = card.querySelector('.table-product-card__toggle');

  if (!glbSrc) {
    setStatus('이 상품에는 3D 모델이 없습니다.');
    return;
  }

  try {
    toggleButton.disabled = true;
    setStatus(`${productName} 모델을 불러오는 중입니다.`);

    const model = await loadGLB(glbSrc, { brighten: true });
    model.name = productName;
    model.userData.productId = productId;

    normalizeModel(model, 1.2);
    const slot = getNextSlot();
    placeOnTable(model, slot.x, tableTopY, slot.z);
    registerDraggableFood(model);
    saveInitialTransform(model);
    scene.add(model);

    addedModelsByProductId.set(productId, model);
    lastSelectedModel = model;
    setProductCardAdded(card, true);
    setStatus(`${productName} 모델이 식탁에 추가되었습니다.`);
  } catch (error) {
    console.error(error);
    setStatus(`${productName} 모델을 불러오지 못했습니다.`);
  } finally {
    toggleButton.disabled = false;
  }
}

function removeProductFromTable(card) {
  const { productId, productName } = card.dataset;
  const model = addedModelsByProductId.get(productId);

  if (!model) {
    setProductCardAdded(card, false);
    return;
  }

  if (selectedModel === model) {
    selectedModel = null;
    isDragging = false;
    controls.enabled = true;
  }

  if (lastSelectedModel === model) {
    lastSelectedModel = null;
  }

  unregisterDraggableFood(model);
  initialPositions.delete(model.uuid);
  addedModelsByProductId.delete(productId);
  scene.remove(model);
  disposeModel(model);
  setProductCardAdded(card, false);
  setStatus(`${productName} 모델을 식탁에서 제거했습니다.`);
}

function setProductCardAdded(card, isAdded) {
  const toggleButton = card.querySelector('.table-product-card__toggle');
  const icon = toggleButton.querySelector('i');
  const productName = card.dataset.productName || '상품';

  card.classList.toggle('is-added', isAdded);
  toggleButton.setAttribute('aria-label', `${productName} ${isAdded ? '제거' : '추가'}`);
  icon.className = `bi ${isAdded ? 'bi-dash-lg' : 'bi-plus-lg'}`;
}

function getNextSlot() {
  const slots = [
    { x: -1.8, z: -0.7 },
    { x: -0.6, z: 0.55 },
    { x: 0.65, z: -0.35 },
    { x: 1.75, z: 0.65 },
    { x: 0, z: -1.25 },
    { x: 1.15, z: -1.15 },
    { x: -1.15, z: 1.15 },
    { x: 0.2, z: 1.25 },
  ];

  const slot = slots[nextSlotIndex % slots.length];
  nextSlotIndex += 1;
  return slot;
}

function normalizeModel(model, targetSize = 1) {
  const box = new THREE.Box3().setFromObject(model);
  const size = new THREE.Vector3();
  box.getSize(size);

  const maxAxis = Math.max(size.x, size.y, size.z);
  if (maxAxis === 0) {
    return;
  }

  const scale = targetSize / maxAxis;
  model.scale.multiplyScalar(scale);

  const newBox = new THREE.Box3().setFromObject(model);
  const center = new THREE.Vector3();
  newBox.getCenter(center);

  model.position.x -= center.x;
  model.position.z -= center.z;
}

function getModelTopY(model) {
  const box = new THREE.Box3().setFromObject(model);
  return box.max.y;
}

function placeOnTable(model, x, y, z) {
  model.position.x = x;
  model.position.z = z;

  const box = new THREE.Box3().setFromObject(model);
  const padding = 0.04;
  model.position.y += y - box.min.y + padding;
}

function registerDraggableFood(model) {
  model.userData.isFood = true;
  foodModels.push(model);

  model.traverse((child) => {
    if (child.isMesh) {
      child.userData.draggableRoot = model;
      draggableObjects.push(child);
    }
  });
}

function unregisterDraggableFood(model) {
  removeFromArray(foodModels, model);

  model.traverse((child) => {
    if (child.isMesh) {
      removeFromArray(draggableObjects, child);
      delete child.userData.draggableRoot;
    }
  });
}

function removeFromArray(items, item) {
  const index = items.indexOf(item);

  if (index >= 0) {
    items.splice(index, 1);
  }
}

function disposeModel(model) {
  const disposedTextures = new Set();

  model.traverse((child) => {
    if (!child.isMesh) {
      return;
    }

    child.geometry?.dispose();

    const materials = Array.isArray(child.material) ? child.material : [child.material];
    materials.forEach((material) => disposeMaterial(material, disposedTextures));
  });
}

function disposeMaterial(material, disposedTextures) {
  if (!material) {
    return;
  }

  Object.values(material).forEach((value) => {
    if (!value?.isTexture || disposedTextures.has(value)) {
      return;
    }

    value.dispose();
    disposedTextures.add(value);
  });

  material.dispose();
}

function saveInitialTransform(model) {
  initialPositions.set(model.uuid, {
    position: model.position.clone(),
    rotation: model.rotation.clone(),
    scale: model.scale.clone(),
  });
}

function resetFoodPositions() {
  foodModels.forEach((model) => {
    const initial = initialPositions.get(model.uuid);

    if (!initial) {
      return;
    }

    model.position.copy(initial.position);
    model.rotation.copy(initial.rotation);
    model.scale.copy(initial.scale);
  });

  setStatus('식탁 위 모델 위치를 초기화했습니다.');
}

function addLights() {
  const mainLight = new THREE.DirectionalLight(0xffffff, 4.5);
  mainLight.position.set(5, 10, 5);
  mainLight.castShadow = true;
  mainLight.shadow.mapSize.width = 2048;
  mainLight.shadow.mapSize.height = 2048;
  scene.add(mainLight);
}

function addFloor() {
  const geometry = new THREE.PlaneGeometry(20, 20);
  const material = new THREE.MeshStandardMaterial({
    color: 0xffffff,
    roughness: 0.85,
  });

  const floor = new THREE.Mesh(geometry, material);
  floor.rotation.x = -Math.PI / 2;
  floor.position.y = -0.02;
  floor.receiveShadow = true;
  scene.add(floor);
}

function updateMousePosition(event) {
  const rect = renderer.domElement.getBoundingClientRect();
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
}

function onPointerDown(event) {
  updateMousePosition(event);
  raycaster.setFromCamera(mouse, camera);

  const intersects = raycaster.intersectObjects(draggableObjects, true);
  if (intersects.length === 0) {
    return;
  }

  selectedModel = intersects[0].object.userData.draggableRoot;
  lastSelectedModel = selectedModel;

  if (!selectedModel) {
    return;
  }

  isDragging = true;
  controls.enabled = false;
  dragPlane.set(new THREE.Vector3(0, 1, 0), -tableTopY);
  raycaster.ray.intersectPlane(dragPlane, dragPoint);
  dragOffset.copy(selectedModel.position).sub(dragPoint);
  renderer.domElement.style.cursor = 'grabbing';
}

function onPointerMove(event) {
  updateMousePosition(event);

  if (!isDragging || !selectedModel) {
    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(draggableObjects, true);
    renderer.domElement.style.cursor = intersects.length > 0 ? 'grab' : 'default';
    return;
  }

  raycaster.setFromCamera(mouse, camera);
  const hit = raycaster.ray.intersectPlane(dragPlane, dragPoint);

  if (!hit) {
    return;
  }

  selectedModel.position.x = THREE.MathUtils.clamp(dragPoint.x + dragOffset.x, -3.1, 3.1);
  selectedModel.position.z = THREE.MathUtils.clamp(dragPoint.z + dragOffset.z, -2.0, 2.0);
}

function onPointerUp() {
  if (!isDragging) {
    return;
  }

  isDragging = false;
  selectedModel = null;
  controls.enabled = true;
  renderer.domElement.style.cursor = 'default';
}

async function captureScene() {
  if (isCapturing) {
    return;
  }

  try {
    isCapturing = true;
    captureBtn.disabled = true;
    setStatus('캡쳐 이미지를 저장하는 중입니다.');

    controls.update();
    renderer.render(scene, camera);

    const blob = await createCanvasBlob();
    const fileName = createCaptureFileName();
    const formData = new FormData();
    formData.append('captureImage', new File([blob], fileName, { type: 'image/png' }));

    const xsrfToken = (() => {
      const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
      return match ? decodeURIComponent(match[1]) : '';
    })();

    const response = await fetch('/api/table-builder/captures', {
      method: 'POST',
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'X-XSRF-TOKEN': xsrfToken,
      },
      body: formData,
    });

    if (!response.ok) {
      throw new Error('capture upload failed');
    }

    const payload = await response.json();
    if (!payload.isSuccess || !payload.result) {
      throw new Error(payload.message || 'capture upload failed');
    }

    downloadBlob(blob, fileName);
    captureLink.href = payload.result.imgSrc;
    captureLink.hidden = false;
    setStatus(`POSTS #${payload.result.postId}에 캡쳐 이미지가 저장되었습니다.`);
  } catch (error) {
    console.error(error);
    setStatus('캡쳐 저장에 실패했습니다. 로그인 상태와 이미지 서버를 확인해주세요.');
  } finally {
    isCapturing = false;
    captureBtn.disabled = false;
  }
}

function createCanvasBlob() {
  return new Promise((resolve, reject) => {
    renderer.domElement.toBlob((blob) => {
      if (blob) {
        resolve(blob);
      } else {
        reject(new Error('canvas blob unavailable'));
      }
    }, 'image/png');
  });
}

function downloadBlob(blob, fileName) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}

function createCaptureFileName() {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  const hh = String(now.getHours()).padStart(2, '0');
  const mi = String(now.getMinutes()).padStart(2, '0');
  const ss = String(now.getSeconds()).padStart(2, '0');

  return `food-table-${yyyy}${mm}${dd}-${hh}${mi}${ss}.png`;
}

function filterProducts() {
  const query = searchInput.value.trim().toLowerCase();

  productCards.forEach((card) => {
    const name = (card.dataset.productName || '').toLowerCase();
    card.classList.toggle('is-hidden', query !== '' && !name.includes(query));
  });
}

function resizeRenderer() {
  const width = Math.max(container.clientWidth, 320);
  const height = Math.max(container.clientHeight, 420);

  camera.aspect = width / height;
  camera.updateProjectionMatrix();
  renderer.setSize(width, height, false);
}

function animate() {
  requestAnimationFrame(animate);
  controls.update();
  renderer.render(scene, camera);
}

productCards.forEach((card) => {
  const toggleButton = card.querySelector('.table-product-card__toggle');
  toggleButton.addEventListener('click', () => toggleProductOnTable(card));
});

tableModelButtons.forEach((button) => {
  button.addEventListener('click', () => changeTableModel(button));
});

searchInput.addEventListener('input', filterProducts);
resetBtn.addEventListener('click', resetFoodPositions);
captureBtn.addEventListener('click', captureScene);

renderer.domElement.addEventListener('pointerdown', onPointerDown);
renderer.domElement.addEventListener('pointermove', onPointerMove);
renderer.domElement.addEventListener('pointerup', onPointerUp);
renderer.domElement.addEventListener('pointerleave', onPointerUp);

window.addEventListener('keydown', (event) => {
  if (!lastSelectedModel) {
    return;
  }

  if (event.key === 'q' || event.key === 'Q') {
    lastSelectedModel.rotation.y += 0.15;
  }

  if (event.key === 'e' || event.key === 'E') {
    lastSelectedModel.rotation.y -= 0.15;
  }
});

window.addEventListener('resize', resizeRenderer);

const resizeObserver = new ResizeObserver(resizeRenderer);
resizeObserver.observe(container);
