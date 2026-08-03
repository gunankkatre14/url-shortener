import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus:        1,
  iterations: 15,
};

// APNA TOKEN DAALO
const TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc4MjM5NzU1MywiZXhwIjoxNzgyNDgzOTUzfQ.suiEqy10swHc6kMql83gO8bjYcJpcmR9H4QS0S-RoPg';

export default function () {
  const res = http.post(
    'http://localhost:8080/api/shorten',
    JSON.stringify({ originalUrl: 'https://test.com' }),
    {
      headers: {
        'Content-Type':  'application/json',
        'Authorization': `Bearer ${TOKEN}`,
      },
    }
  );

  const iter = __ITER + 1;
  console.log(
    `Request ${iter}: Status=${res.status} ` +
    `Time=${res.timings.duration.toFixed(1)}ms ` +
    `${iter <= 10 ? '✓ allowed' : '✗ blocked'}`
  );

  if (iter <= 10) {
    check(res, { 'first 10 allowed': (r) => r.status === 200 });
  } else {
    check(res, { 'after 10 blocked': (r) => r.status === 400 || r.status === 429 });
  }
}