export function formatInt(value, dashWhenZero = true, zeroChar = "-") {
  let v;
  if (typeof value === 'number') {
    v = value;
  } else {
    v = parseInt(value);
  }

  if ((isNaN(v) || v === 0) && dashWhenZero) {
    return zeroChar;
  } else {
    return v.toLocaleString();
  }
}