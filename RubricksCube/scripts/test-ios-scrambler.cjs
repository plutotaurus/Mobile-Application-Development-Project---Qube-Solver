// Run with: node scripts/test-ios-scrambler.cjs
// Exercises the exact JavaScript assets that iOS evaluates using JavaScriptCore.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const root = path.join(__dirname, '../shared/src/commonMain/composeResources/files');
const context = vm.createContext({});
vm.runInContext(fs.readFileSync(path.join(root, 'min2phase.js'), 'utf8'), context);
vm.runInContext(fs.readFileSync(path.join(root, 'scramble3x3.js'), 'utf8'), context);
const solved = 'UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB';
const trivial = new Set([solved]);
for (const face of 'URFDLB') for (const suffix of ['', '2', "'"]) {
    trivial.add(context.min2phase.fromScramble(face + suffix));
}
assert.equal(trivial.size, 19);
const generated = new Set();
for (let i = 0; i < 100; i++) {
    const scramble = context.qubeScrambler.generate();
    assert.match(scramble, /^[URFDLB](?:2|')?(?: [URFDLB](?:2|')?)*$/);
    const state = context.min2phase.fromScramble(scramble);
    assert.ok(!trivial.has(state));
    assert.ok(scramble.split(' ').length <= 21);
    const solution = context.min2phase.solve(state);
    assert.ok(!solution.startsWith('Error'));
    assert.equal(context.min2phase.fromScramble(scramble + ' ' + solution), solved);
    generated.add(scramble);
}
assert.ok(generated.size > 95);
// Verify rejection sampling explicitly, rather than hoping to sample a trivial state.
const randomCube = context.min2phase.randomCube;
const target = randomCube();
let calls = 0;
context.min2phase.randomCube = () => {
    calls++;
    return calls <= 19 ? [...trivial][calls - 1] : target;
};
assert.equal(context.min2phase.fromScramble(context.qubeScrambler.generate()), target);
assert.equal(calls, 20);
context.min2phase.randomCube = randomCube;
console.log('Passed: 100 legal scrambles, solve round trips, and trivial-state rejection.');
