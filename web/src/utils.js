export function formatInt(value, dashWhenZero = true) {
  let v;
  if (typeof value === 'number') {
    v = value;
  } else {
    v = parseInt(value);
  }

  if ((isNaN(v) || v === 0) && dashWhenZero) {
    return '-';
  } else {
    return v.toLocaleString();
  }
}