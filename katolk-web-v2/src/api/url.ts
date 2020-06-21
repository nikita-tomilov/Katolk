export const route = window.location.origin;
export const mainPrefix = "/api";

export const getUrl = (prefix: string = mainPrefix) => `${route}${prefix}`;
