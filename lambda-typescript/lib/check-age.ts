export const handler = async (Country: string): Promise<number> => {
  if (Country=="Germany") {
    return 18;
  }
  else if (Country=="USA") {
    return 21;
  }
  else if (Country=="Sweden") {
    return 18;
  }
  console.log(`Not found country: ${Country}`);
  return -1;
};