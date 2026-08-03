import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const errorRate    = new Rate('error_rate');
const redirectTime = new Trend('redirect_ms', true);
const cacheHits    = new Counter('cache_hits');

export const options = {
  stages: [
    { duration: '15s', target: 20  },
    { duration: '30s', target: 50  },
    { duration: '30s', target: 100 },
    { duration: '15s', target: 0   },
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'],
    http_req_failed:   ['rate<0.01'],
  },
};

// APNA SHORT CODE YAHAN DAALO
const SHORT_CODE = 'bfP3Q0';

export default function () {
  const res = http.get(
    `http://localhost:8080/${SHORT_CODE}`,
    { redirects: 0 }
  );

  redirectTime.add(res.timings.duration);

  if (res.timings.duration < 15) {
    cacheHits.add(1);
  }

  const passed = check(res, {
    'status is 302':       (r) => r.status === 302,
    'has Location header': (r) => r.headers['Location'] !== undefined,
    'under 200ms':         (r) => r.timings.duration < 200,
    'under 50ms':          (r) => r.timings.duration < 50,
    'under 15ms':          (r) => r.timings.duration < 15,
  });

  errorRate.add(!passed);
  sleep(0.1);
}

export function handleSummary(data) {
  const d = data.metrics;
  console.log('\n========== REDIRECT TEST RESULTS ==========');
  console.log(`Total requests:     ${d.http_reqs.values.count}`);
  console.log(`Requests/sec:       ${d.http_reqs.values.rate.toFixed(1)}`);
  console.log(`Avg latency:        ${d.http_req_duration.values.avg.toFixed(1)}ms`);
  console.log(`Median latency:     ${d.http_req_duration.values.med.toFixed(1)}ms`);
  console.log(`p90 latency:        ${d.http_req_duration.values['p(90)'].toFixed(1)}ms`);
  console.log(`p95 latency:        ${d.http_req_duration.values['p(95)'].toFixed(1)}ms`);
  console.log(`p99 latency:        ${d.http_req_duration.values['p(99)'].toFixed(1)}ms`);
  console.log(`Error rate:         ${(d.http_req_failed.values.rate * 100).toFixed(2)}%`);
  if (d.cache_hits) {
    const hits = d.cache_hits.values.count;
    const total = d.http_reqs.values.count;
    console.log(`Cache hits:         ${hits}/${total} (${((hits/total)*100).toFixed(1)}%)`);
  }
  console.log('============================================\n');
  return {};
}