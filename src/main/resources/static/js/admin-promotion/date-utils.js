export function toDateInputValue(date) {
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().split('T')[0];
}

export function todayInputValue() {
  return toDateInputValue(new Date());
}

export function nextMonthInputValue() {
  return toDateInputValue(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000));
}

export function startOfDay(value) {
  const date = new Date(value);
  date.setHours(0, 0, 0, 0);
  return date;
}

export function todayStartOfDay() {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  return date;
}

export function endOfDay(value) {
  const date = new Date(value);
  date.setHours(23, 59, 59, 999);
  return date;
}
